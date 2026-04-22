package com.nex.nexmart.common.constant;

public class RabbitMQConstants {

	// 订单超时取消 - 死信队列相关
	public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
	public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
	public static final String ORDER_DELAY_ROUTING_KEY = "order.delay.routing.key";
	public static final String ORDER_DEAD_EXCHANGE = "order.dead.exchange";
	public static final String ORDER_DEAD_QUEUE = "order.dead.queue";
	public static final String ORDER_DEAD_ROUTING_KEY = "order.dead.routing.key";

	// 秒杀异步下单
	public static final String SECKILL_ORDER_EXCHANGE = "seckill.order.exchange";
	public static final String SECKILL_PRODUCT_ORDER_QUEUE = "seckill.product.order.queue";
	public static final String SECKILL_PRODUCT_ORDER_ROUTING_KEY = "seckill.product.order.routing.key";
	public static final String SECKILL_COUPON_ORDER_QUEUE = "seckill.coupon.order.queue";
	public static final String SECKILL_COUPON_ORDER_ROUTING_KEY = "seckill.coupon.order.routing.key";

	private RabbitMQConstants() {
		throw new AssertionError("不可实例化");
	}
}
