package com.nex.nexmart.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {

    private Long id;
    private Long productId;
    private String productName;
    private String coverUrl;

    private Integer stock;
    private Integer quantity;
    /** 小计 = price × quantity */

	private  Long specId;
	private  String specName;

	private String promotionName;       // 活动名，null表示无活动
	private BigDecimal discountedPrice; // 折后单价，null表示无活动
	private BigDecimal discountedAmount; // 折后总价，null表示无活动
	private BigDecimal subtotal; //合计原价
	private BigDecimal price;//原单价

	private Long categoryId;

}
