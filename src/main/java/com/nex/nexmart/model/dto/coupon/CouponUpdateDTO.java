package com.nex.nexmart.model.dto.coupon;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponUpdateDTO {
	@NotNull
	private Long id;

	// 有用户领取后只允许改这三个字段
	private String name;
	private LocalDateTime receiveEnd;
	private Integer status;

	// 无人领取时可改的字段
	private Integer couponType;
	private Integer discountType;
	private BigDecimal minAmount;
	private BigDecimal discountAmount;
	private BigDecimal discountRate;
	private Integer scope;
	private Long scopeId;
	private Integer total;
	private Integer perLimit;
	private LocalDateTime receiveStart;
	private Integer validDays;
}
