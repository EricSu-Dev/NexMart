package com.nex.nexmart.common.constant;

public class OrderStatusConstants {
	/**
	 * 0-取消
	 */
	public static final int CANCELLED = 0;

	/**
	 * 1-待付款
	 */
	public static final int PENDING_PAYMENT = 1;

	/**
	 * 2-待发货
	 */
	public static final int PENDING_DELIVERY = 2;

	/**
	 * 3-待收货
	 */
	public static final int PENDING_RECEIPT = 3;

	/**
	 * 4-完成
	 */
	public static final int COMPLETED = 4;

	// 私有构造，禁止实例化
	private OrderStatusConstants() {
		throw new AssertionError("不可实例化");
	}
}
