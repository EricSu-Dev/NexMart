package com.nex.nexmart.rabbit.mq.consumer;

import com.nex.nexmart.rabbit.mq.message.SeckillProductOrderMessage;
import com.nex.nexmart.service.impl.order.OrderServiceImpl;
import com.nex.nexmart.service.impl.seckill.SeckillOrderServiceImpl;
import com.nex.nexmart.websocket.CsWebSocketSessionManager;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderMQConsumerTest {

	@Test
	void duplicatedSeckillProductMessageIsAckedWithoutCreatingOrderAgain() throws Exception {
		OrderServiceImpl orderService = mock(OrderServiceImpl.class);
		@SuppressWarnings("unchecked")
		RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		CsWebSocketSessionManager sessionManager = mock(CsWebSocketSessionManager.class);
		SeckillOrderServiceImpl seckillOrderService = mock(SeckillOrderServiceImpl.class);
		RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
		OrderMQConsumer consumer = new OrderMQConsumer(
				orderService,
				redisTemplate,
				sessionManager,
				seckillOrderService,
				rabbitTemplate);
		Channel channel = mock(Channel.class);
		SeckillProductOrderMessage message = new SeckillProductOrderMessage();
		message.setMessageId("msg-1");
		message.setUserId(7L);
		message.setSeckillItemId(88L);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(1L), eq(TimeUnit.DAYS))).thenReturn(false);

		consumer.handleSeckillProductOrder(message, channel, 123L, null);

		verify(seckillOrderService, never()).createProductOrderAsync(message);
		verify(channel).basicAck(123L, false);
	}
}
