package com.nex.nexmart.model.vo.cs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CsProductCardVO {
	private Long productId;
	private String name;        // 商品名
	private String coverImage;  // 封面图
	private BigDecimal price;   // 价格（展示用，取最低价或划线价）

	private String promotionName;       // 活动名，null表示无活动
	private BigDecimal discountedPrice; // 折后价，null表示无活动
	private Integer sales;
}
