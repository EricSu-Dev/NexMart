package com.nex.nexmart.service.impl.ai.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.model.entity.ai.AiMessage;
import com.nex.nexmart.model.entity.ai.AiSession;
import com.nex.nexmart.service.intf.ai.history.AiMessageService;
import com.nex.nexmart.mapper.base.AiMessageMapper;
import com.nex.nexmart.service.intf.ai.history.AiSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Eric
*  针对表【ai_message】的数据库操作Service实现
*  2026-04-20 19:32:56
*/
@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements AiMessageService{
	private final AiSessionService aiSessionService;
	private final StringRedisTemplate stringRedisTemplate;
	public static final String AI_CHAT_HISTORY_Prefix = "NexMart:ai:history:";

	public List<AiMessage> getHistoryByUserId(Long userId) {
		AiSession session = aiSessionService.lambdaQuery()
				.eq(AiSession::getUserId, userId)
				.one();
		if (session == null) return List.of();

		return lambdaQuery()
				.eq(AiMessage::getSessionId, session.getId())
				.orderByAsc(AiMessage::getCreatedAt)
				.list();
	}

	public void clearHistory(Long userId) {
		// 1. 删Redis
		String key = AI_CHAT_HISTORY_Prefix + userId;
		stringRedisTemplate.delete(key);

		// 2. 查session
		AiSession session = aiSessionService.lambdaQuery()
						.eq(AiSession::getUserId, userId)
						.one();
		if (session == null) return;

		// 3. 删消息记录
		remove(new LambdaQueryWrapper<AiMessage>()
				.eq(AiMessage::getSessionId, session.getId()));

		// 4. 删session
		aiSessionService.removeById(session.getId());
	}
}




