package com.nex.nexmart.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

//负责感知连接的建立和断开
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

	@Autowired
	private WebSocketSessionManager sessionManager;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessionManager.add(session.getId(), session);
		System.out.println("管理端连接：" + session.getId());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessionManager.remove(session.getId());
		System.out.println("管理端断开：" + session.getId());
	}
}