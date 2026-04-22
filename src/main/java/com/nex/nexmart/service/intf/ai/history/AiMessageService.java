package com.nex.nexmart.service.intf.ai.history;

import com.nex.nexmart.model.entity.ai.AiMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Eric
*  针对表【ai_message】的数据库操作Service
*  2026-04-20 19:32:56
*/
public interface AiMessageService extends IService<AiMessage> {
	List<AiMessage> getHistoryByUserId(Long userId);
	void clearHistory(Long userId);
}
