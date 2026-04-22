package com.nex.nexmart.model.vo.coupon;

import lombok.Data;

@Data
public class CouponStatsVO {
	private Long couponId;
	private String couponName;
	private Integer total;
	private Integer remained;
	private Integer receivedCount;
	private Integer usedCount;
	private Integer expiredCount;
	private Integer unusedCount;
}