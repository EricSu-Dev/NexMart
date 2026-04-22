package com.nex.nexmart.model.dto.coupon;

import lombok.Data;

@Data
public class CouponPageQueryDTO {
	private Integer current = 1;
	private Integer size = 10;
	private String name;
	private Integer couponType;
	private Integer discountType;
	private Integer status;
	private Integer receiveChannel;
	private Boolean noReceiveChannel;// 如果无领取渠道,就为true
}