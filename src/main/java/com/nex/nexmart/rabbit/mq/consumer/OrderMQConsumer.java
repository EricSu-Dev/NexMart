package com.nex.nexmart.rabbit.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.nex.nexmart.common.constant.RabbitMQConstants;
import com.nex.nexmart.common.constant.RedisSeckillConstants;
import com.nex.nexmart.rabbit.mq.message.SeckillCouponMessage;
import com.nex.nexmart.rabbit.mq.message.SeckillProductOrderMessage;
import com.nex.nexmart.service.impl.order.OrderServiceImpl;
import com.nex.nexmart.service.impl.seckill.SeckillOrderServiceImpl;
import com.nex.nexmart.websocket.CsWebSocketSessionManager;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMQConsumer {

	private static final int MAX_CONSUME_RETRY = 3;
	private static final String FAILURE_REASON_HEADER = "x-failure-reason";

	private static final DefaultRedisScript<Long> SECKILL_ROLLBACK_LUA_SCRIPT =
			new DefaultRedisScript<>(RedisSeckillConstants.SECKILL_ROLLBACK_LUA, Long.class);

	private final OrderServiceImpl orderService;
	private final RedisTemplate<String, String> redisTemplate;
	private final CsWebSocketSessionManager sessionManager;
	private final SeckillOrderServiceImpl seckillOrderService;
	private final RabbitTemplate rabbitTemplate;

	@RabbitListener(queues = RabbitMQConstants.ORDER_DEAD_QUEUE)
	public void handleOrderTimeout(Map<String, Object> msg, Channel channel,
	                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
	                               @Header(name = RabbitMQConstants.MQ_RETRY_COUNT_HEADER, required = false) Integer retryCount)
			throws IOException {
		Long orderId = null;
		try {
			orderId = Long.valueOf(msg.get("orderId").toString());
			log.info("[OrderTimeout] received timeout message orderId={} retryCount={}", orderId, retryCountOrZero(retryCount));
			orderService.cancelOrderByTimeout(orderId);
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[OrderTimeout] consume failed orderId={} retryCount={}", orderId, retryCountOrZero(retryCount), e);
			try {
				handleOrderTimeoutFailure(msg, retryCount, e);
				channel.basicAck(deliveryTag, false);
			} catch (Exception retryException) {
				log.error("[OrderTimeout] retry dispatch failed, message will be requeued orderId={}", orderId, retryException);
				channel.basicNack(deliveryTag, false, true);
			}
		}
	}

	@RabbitListener(queues = RabbitMQConstants.SECKILL_COUPON_ORDER_QUEUE)
	public void handleSeckillCouponOrder(SeckillCouponMessage message, Channel channel,
	                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
	                                     @Header(name = RabbitMQConstants.MQ_RETRY_COUNT_HEADER, required = false) Integer retryCount)
			throws IOException {
		Long userId = message.getUserId();
		Long seckillItemId = message.getSeckillItemId();
		log.info("[SeckillMQ] received coupon order userId={} seckillItemId={} messageId={} retryCount={}",
				userId, seckillItemId, message.getMessageId(), retryCountOrZero(retryCount));

		String dedupKey = RedisSeckillConstants.SECKILL_MSG_DEDUP + message.getMessageId();
		Boolean firstTime = redisTemplate.opsForValue()
				.setIfAbsent(dedupKey, "1", 1, java.util.concurrent.TimeUnit.DAYS);
		if (!Boolean.TRUE.equals(firstTime)) {
			log.warn("[SeckillMQ] duplicate coupon message skipped userId={} seckillItemId={} messageId={}",
					userId, seckillItemId, message.getMessageId());
			channel.basicAck(deliveryTag, false);
			return;
		}

		try {
			seckillOrderService.createCouponOrderAsync(userId, seckillItemId);
			sendSeckillResult(userId, true, "抢券成功");
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[SeckillMQ] coupon order failed userId={} seckillItemId={} messageId={}",
					userId, seckillItemId, message.getMessageId(), e);
			redisTemplate.delete(dedupKey);
			try {
				handleSeckillFailure(
						message,
						userId,
						seckillItemId,
						retryCount,
						RabbitMQConstants.SECKILL_COUPON_ORDER_RETRY_ROUTING_KEY,
						RabbitMQConstants.SECKILL_COUPON_ORDER_FAILED_ROUTING_KEY,
						() -> rollbackSeckillStock(
								RedisSeckillConstants.SECKILL_COUPON_STOCK + seckillItemId,
								RedisSeckillConstants.SECKILL_COUPON_USERS + seckillItemId,
								userId),
						"抢券失败，请重试",
						e);
				channel.basicAck(deliveryTag, false);
			} catch (Exception retryException) {
				log.error("[SeckillMQ] coupon retry dispatch failed, message will be requeued userId={} seckillItemId={}",
						userId, seckillItemId, retryException);
				channel.basicNack(deliveryTag, false, true);
			}
		}
	}

	@RabbitListener(queues = RabbitMQConstants.SECKILL_PRODUCT_ORDER_QUEUE)
	public void handleSeckillProductOrder(SeckillProductOrderMessage message, Channel channel,
	                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
	                                      @Header(name = RabbitMQConstants.MQ_RETRY_COUNT_HEADER, required = false) Integer retryCount)
			throws IOException {
		Long userId = message.getUserId();
		Long seckillItemId = message.getSeckillItemId();
		log.info("[SeckillMQ] received product order userId={} seckillItemId={} messageId={} retryCount={}",
				userId, seckillItemId, message.getMessageId(), retryCountOrZero(retryCount));

		String dedupKey = RedisSeckillConstants.SECKILL_MSG_DEDUP + message.getMessageId();
		Boolean firstTime = redisTemplate.opsForValue()
				.setIfAbsent(dedupKey, "1", 1, java.util.concurrent.TimeUnit.DAYS);
		if (!Boolean.TRUE.equals(firstTime)) {
			log.warn("[SeckillMQ] duplicate product message skipped userId={} seckillItemId={} messageId={}",
					userId, seckillItemId, message.getMessageId());
			channel.basicAck(deliveryTag, false);
			return;
		}

		try {
			seckillOrderService.createProductOrderAsync(message);
			sendSeckillResult(userId, true, "抢购成功");
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[SeckillMQ] product order failed userId={} seckillItemId={} messageId={}",
					userId, seckillItemId, message.getMessageId(), e);
			redisTemplate.delete(dedupKey);
			try {
				handleSeckillFailure(
						message,
						userId,
						seckillItemId,
						retryCount,
						RabbitMQConstants.SECKILL_PRODUCT_ORDER_RETRY_ROUTING_KEY,
						RabbitMQConstants.SECKILL_PRODUCT_ORDER_FAILED_ROUTING_KEY,
						() -> rollbackSeckillStock(
								RedisSeckillConstants.SECKILL_PRODUCT_STOCK + seckillItemId,
								RedisSeckillConstants.SECKILL_PRODUCT_USERS + seckillItemId,
								userId),
						"抢购失败，请重试",
						e);
				channel.basicAck(deliveryTag, false);
			} catch (Exception retryException) {
				log.error("[SeckillMQ] product retry dispatch failed, message will be requeued userId={} seckillItemId={}",
						userId, seckillItemId, retryException);
				channel.basicNack(deliveryTag, false, true);
			}
		}
	}

	private void handleOrderTimeoutFailure(Map<String, Object> msg, Integer retryCount, Exception cause) {
		int currentRetry = retryCountOrZero(retryCount);
		if (currentRetry < MAX_CONSUME_RETRY) {
			int nextRetry = currentRetry + 1;
			publishWithRetryHeader(
					RabbitMQConstants.ORDER_TIMEOUT_RETRY_EXCHANGE,
					RabbitMQConstants.ORDER_TIMEOUT_RETRY_ROUTING_KEY,
					msg,
					nextRetry,
					cause);
			log.warn("[OrderTimeout] sent to retry queue retryCount={} msg={}", nextRetry, msg);
			return;
		}

		publishWithRetryHeader(
				RabbitMQConstants.ORDER_TIMEOUT_FAILED_EXCHANGE,
				RabbitMQConstants.ORDER_TIMEOUT_FAILED_ROUTING_KEY,
				msg,
				currentRetry,
				cause);
		log.error("[OrderTimeout] retry exhausted, sent to failed queue msg={}", msg);
	}

	private void handleSeckillFailure(Object message, Long userId, Long seckillItemId, Integer retryCount,
	                                  String retryRoutingKey, String failedRoutingKey,
	                                  Runnable rollbackAction, String userFailMessage, Exception cause) {
		int currentRetry = retryCountOrZero(retryCount);
		if (currentRetry < MAX_CONSUME_RETRY) {
			int nextRetry = currentRetry + 1;
			publishWithRetryHeader(
					RabbitMQConstants.SECKILL_RETRY_EXCHANGE,
					retryRoutingKey,
					message,
					nextRetry,
					cause);
			log.warn("[SeckillMQ] sent to retry queue userId={} seckillItemId={} retryCount={}",
					userId, seckillItemId, nextRetry);
			return;
		}

		try {
			rollbackAction.run();
		} catch (Exception rollbackException) {
			log.error("[SeckillMQ] redis rollback failed userId={} seckillItemId={}", userId, seckillItemId, rollbackException);
		}

		publishWithRetryHeader(
				RabbitMQConstants.SECKILL_FAILED_EXCHANGE,
				failedRoutingKey,
				message,
				currentRetry,
				cause);
		sendSeckillResult(userId, false, userFailMessage);
		log.error("[SeckillMQ] retry exhausted, sent to failed queue userId={} seckillItemId={}", userId, seckillItemId);
	}

	private void rollbackSeckillStock(String stockKey, String userKey, Long userId) {
		redisTemplate.execute(
				SECKILL_ROLLBACK_LUA_SCRIPT,
				Arrays.asList(stockKey, userKey),
				String.valueOf(userId));
	}

	private void sendSeckillResult(Long userId, boolean success, String message) {
		sessionManager.sendToUser(userId, JSON.toJSONString(Map.of(
				"type", "SECKILL_RESULT",
				"success", success,
				"message", message
		)));
	}

	private void publishWithRetryHeader(String exchange, String routingKey, Object payload, int retryCount, Exception cause) {
		rabbitTemplate.convertAndSend(exchange, routingKey, payload, message -> {
			message.getMessageProperties().setHeader(RabbitMQConstants.MQ_RETRY_COUNT_HEADER, retryCount);
			message.getMessageProperties().setHeader(FAILURE_REASON_HEADER, rootMessage(cause));
			return message;
		});
	}

	private int retryCountOrZero(Integer retryCount) {
		return retryCount == null ? 0 : retryCount;
	}

	private String rootMessage(Exception cause) {
		String message = cause.getMessage();
		if (message == null || message.isBlank()) {
			return cause.getClass().getSimpleName();
		}
		return message.length() > 200 ? message.substring(0, 200) : message;
	}
}
