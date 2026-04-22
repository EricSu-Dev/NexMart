package com.nex.nexmart.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 一个连接池，专门管理所有管理端的 WebSocket 连接
//负责存储和广播
@Component
public class WebSocketSessionManager {

	// 保存所有管理端连接
	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	public void add(String sessionId, WebSocketSession session) {
		sessions.put(sessionId, session);
	}

	public void remove(String sessionId) {
		sessions.remove(sessionId);
	}

	// 广播给所有管理端
	public void broadcast(String message) {
		sessions.values().forEach(session -> {
			try {
				if (session.isOpen()) {
					session.sendMessage(new TextMessage(message));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
