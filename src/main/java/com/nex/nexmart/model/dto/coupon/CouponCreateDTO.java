package com.nex.nexmart.model.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponCreateDTO {
	@NotBlank(message = "券名称不能为空")
	private String name;

	@NotNull(message = "券类型不能为空")
	private Integer couponType; // 1=普通商品券 2=秒杀券

	@NotNull(message = "优惠方式不能为空")
	private Integer discountType; // 1=满减 2=折扣 3=无门槛

	private BigDecimal minAmount;      // 满减门槛，满减券必填
	private BigDecimal discountAmount; // 减免金额，满减/无门槛必填
	private BigDecimal discountRate;   // 折扣率，折扣券必填

	private Integer scope;   // 1=全场 2=单分类 3=单商品，商品券必填
	private Long scopeId;    // 分类ID或商品ID，scope=2或3时必填

	@NotNull(message = "发放总量不能为空")
	private Integer total; // -1=不限量

	@NotNull(message = "每人限领数不能为空")
	private Integer perLimit;

	@NotNull(message = "领取开始时间不能为空")
	private LocalDateTime receiveStart;

	@NotNull(message = "领取截止时间不能为空")
	private LocalDateTime receiveEnd;

	@NotNull(message = "有效天数不能为空")
	private Integer validDays;
}