package com.nex.nexmart.model.vo.cs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CsOrderCardVO {
	private Long orderId;
	private String orderNo;         // 订单号
	private Integer status;         // 订单状态
	private String statusDesc;      // 状态描述文字
	private BigDecimal totalAmount; // 订单金额
	private BigDecimal actualTotalAmount;// 实际支付金额
	private String firstItemName;   // 第一个商品名（如"xx手机 等3件商品"）
	private String firstItemImage;  // 第一个商品图
	private Integer itemCount;      // 商品件数
	private String promotionName; //商品名的右边展示活动名

}
