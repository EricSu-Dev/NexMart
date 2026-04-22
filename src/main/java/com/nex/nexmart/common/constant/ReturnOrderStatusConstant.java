package com.nex.nexmart.common.constant;

public class ReturnOrderStatusConstant {
	/**
	 * 0-申请中
	 */
	public static final int APPLYING = 0;

	/**
	 * 1-已批准
	 */
	public static final int APPROVED = 1;

	/**
	 * 2-已拒绝
	 */
	public static final int REJECTED = 2;

	/**
	 * 3-退款处理中
	 */
	public static final int REFUND_PROCESSING = 3;

	/**
	 * 4-已退款
	 */
	public static final int REFUNDED = 4;


	/**
	 * 5-已取消
	 */
	public static final int CANCELED = 5;

	// 私有构造，禁止实例化
	private ReturnOrderStatusConstant() {
		throw new AssertionError("不可实例化");
	}
}
