package com.nex.nexmart.config;

import com.nex.nexmart.websocket.CsWebSocketHandler;
import com.nex.nexmart.websocket.OrderWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private OrderWebSocketHandler orderWebSocketHandler;
	@Autowired
	private CsWebSocketHandler csWebSocketHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(orderWebSocketHandler, "/ws/order")
				.setAllowedOrigins("*");
		registry.addHandler(csWebSocketHandler, "/ws/cs/websocket")
				.setAllowedOrigins("*");
	}
}