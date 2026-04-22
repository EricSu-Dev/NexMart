package com.nex.nexmart.websocket;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.service.intf.cs.CsMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CsWebSocketHandler extends TextWebSocketHandler {

	@Autowired
	private CsWebSocketSessionManager sessionManager;

	@Autowired
	private CsMessageService csMessageService;

	//根据用户角色建立连接
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Long userId = getUserId(session);
		String role = getRole(session);

		if (role.equals(UserRoleConstants.STRING_ROLE_ADMIN) || role.equals(UserRoleConstants.STRING_ROLE_BOSS)) {
			sessionManager.addAdmin(userId, session);
			System.out.println("管理员连接客服WebSocket: " + userId + " role: " + role);
		} else {
			sessionManager.addUser(userId, session);
			System.out.println("用户连接客服WebSocket: " + userId);
		}
	}
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		// 消息收发交给 CsMessageService 处理
		csMessageService.handleMessage(session, message.getPayload());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Long userId = getUserId(session);
		String role = getRole(session);
		if (role.equals(UserRoleConstants.STRING_ROLE_ADMIN) || role.equals(UserRoleConstants.STRING_ROLE_BOSS)) {
			sessionManager.removeAdmin(userId);
		} else {
			sessionManager.removeUser(userId);
		}
	}


	// 从 URL 参数取 userId，如 /ws/cs?userId=1&isAdmin=false
	private Long getUserId(WebSocketSession session) {
		String query = session.getUri().getQuery(); // "userId=1&isAdmin=false"
		return Long.parseLong(extractParam(query, "userId"));
	}

	//  从 URL 参数取getRole
	private String getRole(WebSocketSession session) {
		String query = session.getUri().getQuery();
		return extractParam(query, "role");
	}

	private String extractParam(String query, String key) {
		for (String param : query.split("&")) {
			String[] kv = param.split("=");
			if (kv[0].equals(key)) return kv[1];
		}
		return null;
	}
}