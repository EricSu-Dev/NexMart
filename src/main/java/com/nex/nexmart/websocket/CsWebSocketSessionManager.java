package com.nex.nexmart.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CsWebSocketSessionManager {

	// key: userId, value: WebSocketSession
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	// key: adminId, value: WebSocketSession
	private final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

	public void addUser(Long userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	public void addAdmin(Long adminId, WebSocketSession session) {
		adminSessions.put(adminId, session);
	}

	public void removeUser(Long userId) {
		userSessions.remove(userId);
	}

	public void removeAdmin(Long adminId) {
		adminSessions.remove(adminId);
	}

	// 发消息给指定用户
	public void sendToUser(Long userId, String message) {
		sendIfOpen(userSessions.get(userId), message);
	}

	// 广播给所有在线管理员
	public void broadcastToAdmins(String message) {
		adminSessions.values().forEach(session -> sendIfOpen(session, message));
	}

	private void sendIfOpen(WebSocketSession session, String message) {
		if (session != null && session.isOpen()) {
			try {
				session.sendMessage(new TextMessage(message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}