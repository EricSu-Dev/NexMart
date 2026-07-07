package com.nex.nexmart.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.nexmart.common.IntentResult;
import com.nex.nexmart.mapper.base.AiMessageMapper;
import com.nex.nexmart.mapper.base.AiSessionMapper;
import com.nex.nexmart.model.entity.ai.AiMessage;
import com.nex.nexmart.model.entity.ai.AiSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

	private final ChatClient chatClient;
	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;
	private final AiContextService aiContextService;
	private final AiKnowledgeService aiKnowledgeService;
	private final AiSessionMapper aiSessionMapper;
	private final AiMessageMapper aiMessageMapper;
	private final ThreadPoolExecutor nexmartExecutor;

	public static final String AI_CHAT_HISTORY_Prefix = "NexMart:ai:history:";
	public static final long AI_CHAT_TTL = 60; // 分钟
	private static final int MAX_HISTORY = 20; // 最多保留10轮
	private static final String SYSTEM_PROMPT = """
			 你是NexMart商城的AI客服助手，名字叫"Nex"。
			【回答原则】
			- 只回答与NexMart商城相关的问题。
			- 优先依据【业务数据】和【知识库资料】回答，不要编造订单、优惠券、库存、积分等信息。
			- 订单、优惠券、积分、库存、活动等实时数据，以后端查询到的【业务数据】为准。
			- 商城规则、购物流程、退款售后、优惠券、积分、秒杀等说明，以【知识库资料】为准。
			- 回答简洁友好，不超过150字。
			- 无法解决的问题，引导用户点击导航栏"客服中心"联系人工客服。
			- 不要透露你是DeepSeek，你只是"Nex"。
			""";

	public Flux<String> chat(Long userId, String message) {
		String key = AI_CHAT_HISTORY_Prefix + userId;

		// 1. 取出历史消息
		List<Message> history = getHistory(key);

		// 1. 意图识别
		IntentResult intent = recognizeIntent(message);

		// 2. 根据意图查库，并检索商城知识库资料
		String businessContext = buildContext(intent, userId);
		String knowledgeContext = aiKnowledgeService.retrieveKnowledge(message);
		String context = buildReferenceContext(businessContext, knowledgeContext);

		// 2. 构建本次请求的消息列表
		List<Message> messages = new ArrayList<>(history);
		String userContent = context.isEmpty() ? message : message + "\n\n【参考数据】\n" + context;
		messages.add(new UserMessage(userContent));

		// 3. 流式调用，同时收集完整回复
		StringBuilder fullReply = new StringBuilder();

		return chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.messages(messages)
				.stream() //开启流式模式
				.content()//只提取文本内容流
				.concatWith(Flux.just("[DONE]")) // 新增：流结束后追加一个 [DONE] 信号
				.onErrorResume(e -> {
					log.error("AI stream failed, userId={}", userId, e);
					messages.add(new AssistantMessage(
							fullReply.isEmpty() ? "[系统提示] AI 服务暂时不可用，请稍后重试" : fullReply.toString()));
					saveHistory(key, messages);
					CompletableFuture.runAsync(() -> persistToMySQL(userId, message, fullReply.toString()), nexmartExecutor);
					return Flux.just(
							fullReply.isEmpty() ? "[系统提示] AI 服务暂时不可用，请稍后重试" : "",
							"[DONE]");
				})
				.doOnNext(fullReply::append)//实时拼接内容
				.doOnCancel(() -> {
					if (fullReply.isEmpty()) return;
					messages.add(new AssistantMessage(fullReply.toString()));
					saveHistory(key, messages);
					log.warn("AI stream cancelled by client, userId={}, partialReply={}", userId, fullReply);
					CompletableFuture.runAsync(() -> persistToMySQL(userId, message, fullReply.toString()), nexmartExecutor);
				})
				.doOnError(e -> {
					messages.add(new AssistantMessage(
							fullReply.isEmpty() ? "[系统提示] AI 回复中断，请重试" : fullReply.toString()));
					saveHistory(key, messages);
					log.error("AI stream error userId={}, partialReply={}", userId, fullReply, e);
					CompletableFuture.runAsync(() -> persistToMySQL(userId, message, fullReply.toString()), nexmartExecutor);
				})
				.doOnComplete(() -> {
					// 4. 流结束后保存完整历史
					messages.add(new AssistantMessage(fullReply.toString()));
					//保存原始 message 而不是拼了 context 的版本，这样历史记录不会越来越臃肿
					saveHistory(key, messages);
					// 新增：异步存MySQL
					CompletableFuture.runAsync(() -> persistToMySQL(userId, message, fullReply.toString()), nexmartExecutor);
				});
	}

	private void persistToMySQL(Long userId, String message, String replyContent) {
		try {
			AiSession session = aiSessionMapper.selectOne(
					new LambdaQueryWrapper<AiSession>()
							.eq(AiSession::getUserId, userId)
			);
			if (session == null) {
				session = new AiSession();
				session.setUserId(userId);
				aiSessionMapper.insert(session);
			}
			Long sessionId = session.getId();

			AiMessage userMsg = new AiMessage();
			userMsg.setSessionId(sessionId);
			userMsg.setRole(1);
			userMsg.setContent(message);
			aiMessageMapper.insert(userMsg);

			AiMessage aiMsg = new AiMessage();
			aiMsg.setSessionId(sessionId);
			aiMsg.setRole(2);
			aiMsg.setContent(replyContent);
			aiMessageMapper.insert(aiMsg);
		} catch (Exception ex) {
			log.error("AI对话记录存储失败 userId={}, error={}", userId, ex.getMessage(), ex);
		}
	}

	private IntentResult recognizeIntent(String message) {
		String prompt = String.format("""
                用户说："%s"
                判断用户意图，只返回JSON，不要有任何多余文字：
                {"intent":"意图","keyword":"关键词"}
                
                意图只能是以下之一：
                query_product=查询商品
                query_seckill=查询秒杀活动
                query_order=查询我的订单
                query_coupon=查询我的优惠券
                query_points=查询我的积分
                query_promotion=查询促销活动
                general=其他
                
                keyword：query_product时填搜索词，其他意图填null
                注意：如果用户用中文品牌名搜索，keyword同时提供中英文，如"华为 HUAWEI"
                """, message);
		try {
			String result = chatClient.prompt()
					.user(prompt)
					.call()
					.content();
			// 安全处理 null 和清理 markdown
			if (result == null || result.trim().isEmpty()) {
				throw new RuntimeException("AI 返回内容为空，无法解析 IntentResult");
			}
			result = result.replaceAll("```json|```", "").trim();
			return objectMapper.readValue(result, IntentResult.class);
		} catch (Exception e) {
			IntentResult fallback = new IntentResult();
			fallback.setIntent("general");
			return fallback;
		}
	}

	private String buildContext(IntentResult intent, Long userId) {
		if (intent == null || intent.getIntent() == null) {
			return "";
		}
		return switch (intent.getIntent()) {
			case "query_product" -> aiContextService.queryProduct(intent.getKeyword());
			case "query_seckill" -> aiContextService.querySeckill();
			case "query_order" -> aiContextService.queryOrder(userId);
			case "query_coupon" -> aiContextService.queryCoupon(userId);
			case "query_points" -> aiContextService.queryPoints(userId);
			case "query_promotion" -> aiContextService.queryPromotion();
			default -> "";
		};
	}

	private String buildReferenceContext(String businessContext, String knowledgeContext) {
		StringBuilder context = new StringBuilder();
		if (businessContext != null && !businessContext.isBlank()) {
			context.append("【业务数据】\n").append(businessContext).append("\n");
		}
		if (knowledgeContext != null && !knowledgeContext.isBlank()) {
			context.append("【知识库资料】\n").append(knowledgeContext);
		}
		return context.toString().trim();
	}


	private List<Message> getHistory(String key) {
		String json = stringRedisTemplate.opsForValue().get(key);
		if (json == null) return new ArrayList<>();
		try {
			//objectMapper.readValue: json->指定对象(List<Map<String, String>>)
			//new TypeReference<>() {}防止泛型擦除,完整写法:new TypeReference<List<Map<String, String>>>() {}
			List<Map<String, String>> raw = objectMapper.readValue(json, new TypeReference<>() {});

			//返回List<Message>
			return raw.stream()
					.map(m -> {
						String role = m.get("role");
						String content = m.get("content");
						if ("user".equals(role)) {
							return new UserMessage(content);
						} else if ("system".equals(role)) {
							return new SystemMessage(content);
						} else {
							return new AssistantMessage(content);
						}
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private void saveHistory(String key, List<Message> messages) {
		// 超出限制时截断，保留最近20条
		//subList 截取后20条
		List<Message> toSave = messages.size() > MAX_HISTORY
				? messages.subList(messages.size() - MAX_HISTORY, messages.size())
				: messages;
		try {
			List<Map<String, String>> raw = toSave.stream()
					.map(m -> {
						//switch (m): 看 m 这个对象到底是什么类型
						String role = switch (m) {
							case UserMessage ignored  -> "user";
							case SystemMessage ignored  -> "system";
							case AssistantMessage ignored -> "assistant";
							default                -> "assistant";   // 未知类型默认 assistant
						};
						String content = Objects.toString(m.getText(), "");   // 防止 null
						return Map.of("role", role, "content", content);
					})
					.collect(Collectors.toList());
			String json = objectMapper.writeValueAsString(raw);
			stringRedisTemplate.opsForValue().set(key, json, AI_CHAT_TTL, TimeUnit.MINUTES);
		} catch (Exception e) {
			// 存储失败不影响主流程
		}
	}
}
