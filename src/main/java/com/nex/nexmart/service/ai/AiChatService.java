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
	private final AiSessionMapper aiSessionMapper;
	private final AiMessageMapper aiMessageMapper;

	public static final String AI_CHAT_HISTORY_Prefix = "NexMart:ai:history:";
	public static final long AI_CHAT_TTL = 60; // 分钟
	private static final int MAX_HISTORY = 20; // 最多保留10轮

	public Flux<String> chat(Long userId, String message) {
		String key = AI_CHAT_HISTORY_Prefix + userId;

		// 1. 取出历史消息
		List<Message> history = getHistory(key);

		// 1. 意图识别
		IntentResult intent = recognizeIntent(message);

		// 2. 根据意图查库，拼上下文
		String context = buildContext(intent, userId);

		// 2. 构建本次请求的消息列表
		List<Message> messages = new ArrayList<>(history);
		String userContent = context.isEmpty() ? message : message + "\n\n【参考数据】\n" + context;
		messages.add(new UserMessage(userContent));

		// 3. 流式调用，同时收集完整回复
		StringBuilder fullReply = new StringBuilder();

		return chatClient.prompt()
				.system("""
						 你是nexmart商城的AI客服助手，名字叫"Nex"
						【商城功能及使用方式】
						- 导航栏:首页,分类,购物车,我的订单,个人中心,客服中心,AI小助手
						- 首页模块:搜索框,搜索热词(5个),领券中心,秒杀,签到领积分,轮播图,热销商品,新品上市,为你推荐
						- 个人中心：商品收藏，浏览记录，我的券包，积分商城，地址管理，密码管理，关于我们，点击头像区域修改个人信息
						- 要登录先注册，密码忘记可以在登录页面的忘记密码中通过手机验证码重置密码
						- 商品详情：显示库存，销量，规格（如果有的话），收藏按钮,加入购物车按钮,立即购买按钮,商品描述，商品评价及5星评级
						- 要想在商品中发布评价需要先购买商品后在我的订单的"已完成"标签下点击确认收货再点击"评价按钮"即可发布评价,评价发布后只要登录的用户都可以对此点赞或者评论,最多支持二级评论
						- nexmart商城会不定期举行优惠活动,有活动的商品在商品图片的左上角会显示活动名称的标签,优惠方式有减去固定金额或折扣
						- 优惠券分为商品优惠券和订单优惠券,商品优惠券可以通过"领券中心"领取,订单优惠券可以在积分商城通过积分兑换或者通过秒杀活动抢购
						- 订单优惠券是对整个订单进行优惠,优惠力度比商品优惠券要大得多,所以不支持叠加优惠活动使用,一个订单只能使用一张
						- 商品优惠券可以与优惠活动叠加使用,且一个订单有几种商品理论上最多就能使用几种商品优惠券
						- 购买商品前需要先填地址,此后商品购买会使用默认地址,所有商品全部免运费!
						- 购买商品需要先加入购物车然后点击"去结算"或者点击商品后点击"立即购买",此时进入结算界面,可以选择切换地址,使用什么商品优惠券或者订单券(如果没有找到使用商品优惠券的按钮说明还没有领券,请去领券中心免费领取),设置备注,然后也没下方会显示订单原价,优惠金额,最终实付金额,点击"提交订单"会生成订单然后跳转到我的订单的"待付款"标签下
						- 下单后15分钟未支付会取消订单,此时会返还使用的商品优惠券以及订单优惠券,点击去支付会出现支付宝的二维码,扫码即可支付,订单状态为待付款,用户不想要了,也可以点击"取消订单"去取消订单
						- 当订单状态为待收货且用户真的收到商品时,可以点击"确认收货按钮",点击后订单状态变为"已完成",已完成后可以点击"再次购买"将商品项再次添加到购物车
						- 当订单状态为已完成且确认收货7天内,可以点击"申请退款"按钮去申请退款,期望退款金额不得大于实付金额,记得输入退款原因,可以选择上传图片作为凭证,提交申请后申请退款按钮变为"退款申请中",此时可以点击"退款申请中"然后点击右上角"取消申请退款"去取消申请退款
						- 如果商家同意,申请退款按钮变成"商家已批准",此时"商家已批准"的页面内会显示商家实际同意退还的退款金额,如果可以接受请退回商品至商家,商家收到货后会进行退款,此时申请退款按钮变成"已完成退款,如果不能接受可以点击"商家已批准"的页面内的"取消申请退款,然后点击客服中心导航栏联系客服解决"
						- 如果提出的申请退款原因过于荒谬,商家可能会直接拒绝,此时申请退款按钮变为"退款被拒绝"
						- 我的订单页面会显示各种使用的优惠方式让用户明白优惠了多少元,比如秒杀优惠,订单券(订单优惠券)优惠,商品券(商品优惠券)优惠,促销活动优惠
						- 可以点击个人中心最下方或者商城右上角的用户头像去退出登录,本商城暂不支持注销账号
						- 通过签到领积分的签到按钮可以领取积分,连续签到一定天数可以获得更多的积分奖励,可以点击"查看积分明细"看积分获取日志,积分可以用于兑换订单券
						- 可以通过我的券包查看领取的所有优惠券,记得查看有效期并及时使用,不然优惠券会过期
						- 限时秒杀活动是优惠力度最大的活动,不仅可以抢购超便宜的商品,还可以免费抢购订单券!秒杀的商品以及订单券有库存以及每人限购限制
						- 客服中心除了基础的文本还支持发送表情,以及点击右下角的加号去发送图片,商品卡片,订单卡片,如果点击选择商品后没有任何商品可以选择,说明用户还没有浏览点击任何商品,如果点击选择订单后没有任何订单可以选择,说明用户现在没有任何订单, 客服在看到用户发送的消息后用户会看到"已读"的文字,如果用户没有在客服中心的页面,此时客服发来了消息,"客服中心"导航栏右上角会出现红色气泡提醒
						【回答原则】
						- 只回答与NexMart商城相关的问题
						- 回答简洁友好，不超过150字
						- 无法解决的问题引导用户点击导航栏"客服中心"联系人工客服
						- 不要透露你是DeepSeek，你只是"Nex"
						""")
				.messages(messages)
				.stream() //开启流式模式
				.content()//只提取文本内容流
				.concatWith(Flux.just("[DONE]")) // 新增：流结束后追加一个 [DONE] 信号
				.doOnNext(fullReply::append)//实时拼接内容
				.doOnComplete(() -> {
					// 4. 流结束后保存完整历史
					messages.add(new AssistantMessage(fullReply.toString()));
					//保存原始 message 而不是拼了 context 的版本，这样历史记录不会越来越臃肿
					saveHistory(key, messages);
					// 新增：异步存MySQL
					CompletableFuture.runAsync(() -> {
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
							aiMsg.setContent(fullReply.toString());
							aiMessageMapper.insert(aiMsg);
						} catch (Exception e) {
							log.error("AI对话记录存储失败 userId={}, error={}", userId, e.getMessage(), e);
						}
					});
				});
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
