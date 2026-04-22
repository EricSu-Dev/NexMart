package com.nex.nexmart.model.vo.seckill;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillCouponItemVO {
	private Long id;
	private Integer itemType;

	// 券信息
	private Long couponId;
	private String couponName;
	private Integer couponDiscountType;
	private BigDecimal couponDiscountAmount;
	private BigDecimal couponDiscountRate;
	private Integer couponRemained;
	private Integer couponTotal;
	private Integer couponPerLimit;
	private LocalDateTime couponReceiveStart;
	private LocalDateTime couponReceiveEnd;
	private Integer status;

	private String activityName;

	private Boolean purchased;
}
