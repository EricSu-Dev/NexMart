package com.nex.nexmart.model.vo.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AvailableCouponVO {
	private Long userCouponId;
	private Long couponId;
	private Integer couponType;
	private String couponTypeDesc;
	private String name;
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
	private Boolean usable;

}
