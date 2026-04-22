package com.nex.nexmart.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderPreviewVO {
	private BigDecimal originalAmount;    // 原始总价
	private BigDecimal promotionTotalDiscount; // promotion优惠
	private BigDecimal orderCouponDiscount;   // 订单券优惠
	private BigDecimal productCouponTotalDiscount; // 商品券优惠合计,之前是productCouponDiscount
	private Map<Long, BigDecimal> productCouponDiscountMap;//购物车项id,优惠金额
	private BigDecimal finalAmount;       // 最终应付
	private Boolean orderCouponUsable;    // 订单券是否可用
	private String orderCouponUsableReason;// 订单券不可用原因
}
