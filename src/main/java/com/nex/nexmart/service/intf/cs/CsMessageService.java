package com.nex.nexmart.service.intf.cs;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.entity.cs.CsMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.entity.cs.CsSession;
import com.nex.nexmart.model.vo.cs.CsMessageVO;
import com.nex.nexmart.model.vo.cs.CsOrderCardVO;
import com.nex.nexmart.model.vo.cs.CsSessionVO;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

/**
* @author Eric
* @description 针对表【cs_message】的数据库操作Service
* @createDate 2026-04-03 12:47:40
*/
public interface CsMessageService extends IService<CsMessage> {
	void handleMessage(WebSocketSession wsSession, String payload);

	CsSession createSession(Long userId);

	List<CsMessageVO> getMessages(Long sessionId,int readerType);

	void closeSession(Long sessionId);

	PageResult<CsOrderCardVO> getOrderCards(long current, long size, Long userId, Integer status, String keyword);

	Integer getUnreadCount(Long sessionId);

	//============================管理端===============================
	// 1. 查所有会话列表（用户端只能看自己的，管理端要看所有人的）
	PageResult<CsSessionVO> getSessions(long current, long size, String keyword);

}
