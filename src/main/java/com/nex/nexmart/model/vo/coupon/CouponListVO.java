package com.nex.nexmart.model.vo.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponListVO {
	private Long id;
	private String name;
	private Integer couponType;
	private String couponTypeDesc;
	private Integer discountType;
	private String discountTypeDesc;
	private BigDecimal minAmount;
	private BigDecimal discountAmount;
	private BigDecimal discountRate;
	private Integer scope;
	private String scopeDesc;
	private String scopeName;
	private Integer validDays;
	private LocalDateTime receiveEnd;
	private Integer remained;
	private Boolean receivable; // false=已领过/达上限/已抢完
	private Integer perLimit;
}
