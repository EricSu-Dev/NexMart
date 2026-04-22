package com.nex.nexmart.rabbit.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.nex.nexmart.rabbit.mq.message.SeckillCouponMessage;
import com.nex.nexmart.rabbit.mq.message.SeckillProductOrderMessage;
import com.nex.nexmart.common.constant.RabbitMQConstants;
import com.nex.nexmart.common.constant.RedisSeckillConstants;
import com.nex.nexmart.service.impl.order.OrderServiceImpl;
import com.nex.nexmart.service.impl.seckill.SeckillOrderServiceImpl;
import com.nex.nexmart.websocket.CsWebSocketSessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;

import org.springframework.messaging.handler.annotation.Header;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMQConsumer {
	private final OrderServiceImpl orderService;
	private final RedisTemplate<String, String> redisTemplate;
	private final CsWebSocketSessionManager sessionManager;
	private final SeckillOrderServiceImpl seckillOrderService;
	private final RabbitAdmin rabbitAdmin;
	@PostConstruct
	public void testRabbit() {
		rabbitAdmin.initialize();
		System.out.println("RabbitAdmin initialized");
	}

	//Channel channel：用于和 RabbitMQ 进行交互（最重要的是用来确认消息）
	//@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag：每条消息在 Channel 中的唯一编号，必须用来做 ACK/NACK

	@RabbitListener(queues = RabbitMQConstants.ORDER_DEAD_QUEUE)
	public void handleOrderTimeout(Map<String, Object> msg, Channel channel,
	                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
		Long orderId = null;
		try {
			orderId = Long.valueOf(msg.get("orderId").toString());
			log.info("[OrderTimeout] 收到超时取消消息 orderId={}", orderId);
			orderService.cancelOrderByTimeout(orderId);
			channel.basicAck(deliveryTag, false);//告诉 RabbitMQ：这条消息我已经成功处理，可以从队列中删除了,false只确认当前这条消息
		} catch (Exception e) {
			log.error("[OrderTimeout] 处理失败 orderId={}", orderId, e);
			channel.basicNack(deliveryTag, false, false);//处理失败,第二个false代表不不重新入队,防止死循环
		}
	}

	@RabbitListener(queues = RabbitMQConstants.SECKILL_COUPON_ORDER_QUEUE)
	public void handleSeckillCouponOrder(SeckillCouponMessage message, Channel channel,
	                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
		Long userId = message.getUserId();
		Long seckillItemId = message.getSeckillItemId();
		log.info("[SeckillMQ] 收到秒杀券下单消息 userId={} seckillItemId={}", userId, seckillItemId);

		try {
			seckillOrderService.createCouponOrderAsync(userId, seckillItemId);
			sessionManager.sendToUser(userId, JSON.toJSONString(Map.of(
					"type", "SECKILL_RESULT",
					"success", true,
					"message", "抢券成功"
			)));
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[SeckillMQ] 秒杀券下单失败 userId={} seckillItemId={}", userId, seckillItemId, e);
			// 回滚Redis
			String stockKey = RedisSeckillConstants.SECKILL_COUPON_STOCK + seckillItemId;
			String userKey = RedisSeckillConstants.SECKILL_COUPON_USERS + seckillItemId;
			redisTemplate.opsForValue().increment(stockKey);
			redisTemplate.opsForHash().increment(userKey, String.valueOf(userId), -1);
			sessionManager.sendToUser(userId, JSON.toJSONString(Map.of(
					"type", "SECKILL_RESULT",
					"success", false,
					"message", "抢券失败，请重试"
			)));
			channel.basicNack(deliveryTag, false, false);
		}
	}
	@RabbitListener(queues = RabbitMQConstants.SECKILL_PRODUCT_ORDER_QUEUE)
	public void handleSeckillProductOrder(SeckillProductOrderMessage message, Channel channel,
	                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
		Long userId = message.getUserId();
		Long seckillItemId = message.getSeckillItemId();
		log.info("[SeckillMQ] 收到秒杀商品下单消息 userId={} seckillItemId={}", userId, seckillItemId);

		try {
			seckillOrderService.createProductOrderAsync(message);
			sessionManager.sendToUser(userId, JSON.toJSONString(Map.of(
					"type", "SECKILL_RESULT",
					"success", true,
					"message", "抢购成功"
			)));
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[SeckillMQ] 秒杀商品下单失败 userId={} seckillItemId={}", userId, seckillItemId, e);
			// 回滚Redis
			String stockKey = RedisSeckillConstants.SECKILL_PRODUCT_STOCK + seckillItemId;
			String userKey = RedisSeckillConstants.SECKILL_PRODUCT_USERS + seckillItemId;
			redisTemplate.opsForValue().increment(stockKey);
			redisTemplate.opsForHash().increment(userKey, String.valueOf(userId), -1);
			sessionManager.sendToUser(userId, JSON.toJSONString(Map.of(
					"type", "SECKILL_RESULT",
					"success", false,
					"message", "抢购失败，请重试"
			)));
			channel.basicNack(deliveryTag, false, false);
		}
	}
}
