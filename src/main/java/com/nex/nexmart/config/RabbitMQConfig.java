package com.nex.nexmart.config;

import com.nex.nexmart.common.constant.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RabbitMQConfig {

	private RabbitTemplate rabbitTemplate;
	private static final String PENDING_MESSAGE_ID_HEADER = "x-pending-message-id";

	// 重试线程池
	private final ScheduledExecutorService retryExecutor =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "mq-retry");
				t.setDaemon(true);
				return t;
			});

	// 待确认消息（用于 nack 时重发）
	private final ConcurrentHashMap<String, PendingMessage> pendingMap = new ConcurrentHashMap<>();

	private static class PendingMessage {
		final String exchange;
		final String routingKey;
		final Object payload;
		final Runnable onExhausted; // 重试耗尽后的回调（如 Redis 回滚）
		int retries;

		PendingMessage(String exchange, String routingKey, Object payload, Runnable onExhausted) {
			this.exchange = exchange;
			this.routingKey = routingKey;
			this.payload = payload;
			this.onExhausted = onExhausted;
		}
	}

	// ==================== 死信队列（订单超时取消）====================

	// 延迟交换机（消息先到这里，TTL过期后转发到死信交换机）
	@Bean
	public DirectExchange orderDelayExchange() {
		return new DirectExchange(RabbitMQConstants.ORDER_DELAY_EXCHANGE);
	}

	// 延迟队列（设置TTL + 死信转发目标）
	@Bean
	public Queue orderDelayQueue() {
		return QueueBuilder.durable(RabbitMQConstants.ORDER_DELAY_QUEUE)
				.ttl(15 * 60 * 1000)                        // 15分钟超时
				.deadLetterExchange(RabbitMQConstants.ORDER_DEAD_EXCHANGE)
				.deadLetterRoutingKey(RabbitMQConstants.ORDER_DEAD_ROUTING_KEY)
				.build();
	}

	// 死信交换机（接收过期消息）
	@Bean
	public DirectExchange orderDeadExchange() {
		return new DirectExchange(RabbitMQConstants.ORDER_DEAD_EXCHANGE);
	}

	// 死信队列（消费者监听这里，执行取消订单逻辑）
	@Bean
	public Queue orderDeadQueue() {
		return QueueBuilder.durable(RabbitMQConstants.ORDER_DEAD_QUEUE).build();
	}

	// Binding
	@Bean
	public Binding orderDelayBinding() {
		return BindingBuilder.bind(orderDelayQueue())
				.to(orderDelayExchange())
				.with(RabbitMQConstants.ORDER_DELAY_ROUTING_KEY);
	}

	@Bean
	public Binding orderDeadBinding() {
		return BindingBuilder.bind(orderDeadQueue())
				.to(orderDeadExchange())
				.with(RabbitMQConstants.ORDER_DEAD_ROUTING_KEY);
	}

	@Bean
	public DirectExchange orderTimeoutRetryExchange() {
		return new DirectExchange(RabbitMQConstants.ORDER_TIMEOUT_RETRY_EXCHANGE);
	}

	@Bean
	public Queue orderTimeoutRetryQueue() {
		return QueueBuilder.durable(RabbitMQConstants.ORDER_TIMEOUT_RETRY_QUEUE)
				.ttl(10 * 1000)
				.deadLetterExchange(RabbitMQConstants.ORDER_DEAD_EXCHANGE)
				.deadLetterRoutingKey(RabbitMQConstants.ORDER_DEAD_ROUTING_KEY)
				.build();
	}

	@Bean
	public Binding orderTimeoutRetryBinding() {
		return BindingBuilder.bind(orderTimeoutRetryQueue())
				.to(orderTimeoutRetryExchange())
				.with(RabbitMQConstants.ORDER_TIMEOUT_RETRY_ROUTING_KEY);
	}

	@Bean
	public DirectExchange orderTimeoutFailedExchange() {
		return new DirectExchange(RabbitMQConstants.ORDER_TIMEOUT_FAILED_EXCHANGE);
	}

	@Bean
	public Queue orderTimeoutFailedQueue() {
		return QueueBuilder.durable(RabbitMQConstants.ORDER_TIMEOUT_FAILED_QUEUE).build();
	}

	@Bean
	public Binding orderTimeoutFailedBinding() {
		return BindingBuilder.bind(orderTimeoutFailedQueue())
				.to(orderTimeoutFailedExchange())
				.with(RabbitMQConstants.ORDER_TIMEOUT_FAILED_ROUTING_KEY);
	}

	@Bean
	public DirectExchange seckillOrderExchange() {
		return new DirectExchange(RabbitMQConstants.SECKILL_ORDER_EXCHANGE);
	}

	@Bean
	public DirectExchange seckillRetryExchange() {
		return new DirectExchange(RabbitMQConstants.SECKILL_RETRY_EXCHANGE);
	}

	@Bean
	public DirectExchange seckillFailedExchange() {
		return new DirectExchange(RabbitMQConstants.SECKILL_FAILED_EXCHANGE);
	}

	@Bean
	public Queue seckillProductOrderQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_PRODUCT_ORDER_QUEUE).build();
	}

	@Bean
	public Binding seckillProductOrderBinding() {
		return BindingBuilder.bind(seckillProductOrderQueue())
				.to(seckillOrderExchange())
				.with(RabbitMQConstants.SECKILL_PRODUCT_ORDER_ROUTING_KEY);
	}

	@Bean
	public Queue seckillCouponOrderQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_COUPON_ORDER_QUEUE).build();
	}

	@Bean
	public Binding seckillCouponOrderBinding() {
		return BindingBuilder.bind(seckillCouponOrderQueue())
				.to(seckillOrderExchange())
				.with(RabbitMQConstants.SECKILL_COUPON_ORDER_ROUTING_KEY);
	}

	@Bean
	public Queue seckillProductOrderRetryQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_PRODUCT_ORDER_RETRY_QUEUE)
				.ttl(5 * 1000)
				.deadLetterExchange(RabbitMQConstants.SECKILL_ORDER_EXCHANGE)
				.deadLetterRoutingKey(RabbitMQConstants.SECKILL_PRODUCT_ORDER_ROUTING_KEY)
				.build();
	}

	@Bean
	public Binding seckillProductOrderRetryBinding() {
		return BindingBuilder.bind(seckillProductOrderRetryQueue())
				.to(seckillRetryExchange())
				.with(RabbitMQConstants.SECKILL_PRODUCT_ORDER_RETRY_ROUTING_KEY);
	}

	@Bean
	public Queue seckillCouponOrderRetryQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_COUPON_ORDER_RETRY_QUEUE)
				.ttl(5 * 1000)
				.deadLetterExchange(RabbitMQConstants.SECKILL_ORDER_EXCHANGE)
				.deadLetterRoutingKey(RabbitMQConstants.SECKILL_COUPON_ORDER_ROUTING_KEY)
				.build();
	}

	@Bean
	public Binding seckillCouponOrderRetryBinding() {
		return BindingBuilder.bind(seckillCouponOrderRetryQueue())
				.to(seckillRetryExchange())
				.with(RabbitMQConstants.SECKILL_COUPON_ORDER_RETRY_ROUTING_KEY);
	}

	@Bean
	public Queue seckillProductOrderFailedQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_PRODUCT_ORDER_FAILED_QUEUE).build();
	}

	@Bean
	public Binding seckillProductOrderFailedBinding() {
		return BindingBuilder.bind(seckillProductOrderFailedQueue())
				.to(seckillFailedExchange())
				.with(RabbitMQConstants.SECKILL_PRODUCT_ORDER_FAILED_ROUTING_KEY);
	}

	@Bean
	public Queue seckillCouponOrderFailedQueue() {
		return QueueBuilder.durable(RabbitMQConstants.SECKILL_COUPON_ORDER_FAILED_QUEUE).build();
	}

	@Bean
	public Binding seckillCouponOrderFailedBinding() {
		return BindingBuilder.bind(seckillCouponOrderFailedQueue())
				.to(seckillFailedExchange())
				.with(RabbitMQConstants.SECKILL_COUPON_ORDER_FAILED_ROUTING_KEY);
	}

	//在应用启动时，自动把代码里声明的队列/交换机/绑定创建到 RabbitMQ 服务器上。
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	//消息序列化器，把 Java 对象转成 JSON 格式发送，消费者收到后再反序列化回 Java 对象
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 配置 RabbitTemplate，保证生产者到 Broker 的消息不丢失
	 * - Confirm 机制：消息未到达 Broker 时回调 nack，记录日志（生产环境应加重发/补偿）
	 * - Return 机制：消息到达交换机但路由不到队列时回调，记录日志
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		rabbitTemplate.setMandatory(true);

		// 开启 Confirm：ack=false 时重试最多3次，间隔 2s/4s/6s
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (ack && correlationData != null) {
				pendingMap.remove(correlationData.getId());
			}
			if (!ack) {
				handleConfirmNack(correlationData, cause);
			}
		});

		// 开启 Return
		rabbitTemplate.setReturnsCallback(this::handleReturnedMessage);
		this.rabbitTemplate = rabbitTemplate;
		return rabbitTemplate;
	}

	private void handleReturnedMessage(ReturnedMessage returned) {
		Object correlationId = returned.getMessage().getMessageProperties()
				.getHeaders().get(PENDING_MESSAGE_ID_HEADER);
		if (correlationId == null) {
			log.error("[RabbitMQ] 消息路由失败且缺少pending id exchange={} routingKey={} message={}",
					returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
			return;
		}

		PendingMessage pm = pendingMap.remove(correlationId.toString());
		log.error("[RabbitMQ] 消息路由失败 exchange={} routingKey={} replyCode={} replyText={}",
				returned.getExchange(), returned.getRoutingKey(),
				returned.getReplyCode(), returned.getReplyText());
		if (pm != null && pm.onExhausted != null) {
			pm.onExhausted.run();
		}
	}

	private void handleConfirmNack(CorrelationData correlationData, String cause) {
		if (correlationData == null) return;
		PendingMessage pm = pendingMap.get(correlationData.getId());
		if (pm == null) return;

		if (pm.retries >= 3) {
			log.error("[RabbitMQ] 重试3次仍失败 exchange={} routingKey={} cause={}",
					pm.exchange, pm.routingKey, cause);
			pendingMap.remove(correlationData.getId());
			if (pm.onExhausted != null) {
				pm.onExhausted.run();
			}
			return;
		}
		pm.retries++;
		long delay = pm.retries * 2L; // 2s / 4s / 6s
		log.warn("[RabbitMQ] Confirm nack，第{}次重试 exchange={} routingKey={} delay={}s",
				pm.retries, pm.exchange, pm.routingKey, delay);
		retryExecutor.schedule(() -> {
			sendPendingMessage(correlationData.getId(), pm);
		}, delay, TimeUnit.SECONDS);
	}

	private void sendPendingMessage(String correlationId, PendingMessage pendingMessage) {
		rabbitTemplate.convertAndSend(
				pendingMessage.exchange,
				pendingMessage.routingKey,
				pendingMessage.payload,
				message -> {
					message.getMessageProperties().setHeader(PENDING_MESSAGE_ID_HEADER, correlationId);
					return message;
				},
				new CorrelationData(correlationId));
	}

	/**
	 * 发送订单超时取消延迟消息（带 Confirm 重试，有定时兜底无 rollback）
	 */
	public void sendOrderTimeoutMessage(Long orderId) {
		Map<String, Object> msg = new HashMap<>();
		msg.put("orderId", orderId);
		String correlationId = UUID.randomUUID().toString();
		// 超时取消有定时扫描兜底，无需 onExhausted 回调
		pendingMap.put(correlationId, new PendingMessage(
				RabbitMQConstants.ORDER_DELAY_EXCHANGE,
				RabbitMQConstants.ORDER_DELAY_ROUTING_KEY,
				msg, null));
		sendPendingMessage(correlationId, pendingMap.get(correlationId));
	}

	/**
	 * 发送秒杀异步下单消息（带 Confirm 重试，重试耗尽后触发 onExhausted 回滚 Redis）
	 */
	public void sendSeckillMessage(String exchange, String routingKey,
	                               Object payload, Runnable onExhausted) {
		String correlationId = UUID.randomUUID().toString();
		pendingMap.put(correlationId, new PendingMessage(
				exchange, routingKey, payload, onExhausted));
		try {
			sendPendingMessage(correlationId, pendingMap.get(correlationId));
		} catch (Exception e) {
			// convertAndSend 同步抛异常 → 立即回滚，不等异步 Confirm
			pendingMap.remove(correlationId);
			if (onExhausted != null) {
				onExhausted.run();
			}
			throw e;
		}
	}
}
