package com.nex.nexmart.model.vo.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponVO {
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
	private Long scopeId;
	private String scopeDesc;
	private String scopeName; // 分类名或商品名，全场时为null

	private Integer total;
	private Integer remained;
	private Integer perLimit;

	private LocalDateTime receiveStart;
	private LocalDateTime receiveEnd;
	//领取渠道：1=领券中心，2=积分商城，3=秒杀
	private Integer receiveChannel;
	private Integer validDays;

	private Integer status;
	private LocalDateTime createdAt;

}
