package com.nex.nexmart.config;

import com.nex.nexmart.common.constant.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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
	public DirectExchange seckillOrderExchange() {
		return new DirectExchange(RabbitMQConstants.SECKILL_ORDER_EXCHANGE);
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

	//在应用启动时，自动把代码里声明的队列/交换机/绑定创建到 RabbitMQ 服务器上。
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
