package com.nex.nexmart.model.vo.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyCouponVO {
	private Long id;        // coupon_user.id
	private Long couponId;
	private String name;
	private Integer couponType;
	private Integer discountType;
	private String discountTypeDesc;
	private BigDecimal minAmount;
	private BigDecimal discountAmount;
	private BigDecimal discountRate;
	private Integer scope;
	private Long scopeId;
	private String scopeDesc;
	private String scopeName;
	private LocalDateTime expireAt;
	private Integer status; // 0=未使用 1=已使用 2=已过期
	private LocalDateTime receivedAt;
}
