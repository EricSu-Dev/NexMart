package com.nex.nexmart.model.vo.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

	private Long id;
	private Long productId;
	private String productName;
	private String coverUrl;

	private Integer quantity;


	/** 是否已评价 */
	private Boolean reviewed;

	/** 评价ID（已评价时） */
	private Long reviewId;

	private String specName;

	private ReturnOrderVO returnOrder;

	private BigDecimal price;//单价
	private BigDecimal promotionalPrice;//活动优惠后单价
	private BigDecimal seckillPrice; //秒杀优惠后单价

	private String promotionName;//活动名，null表示无活动
	private String couponName;//商品券名，null表示无商品券
	private String seckillName;//秒杀活动名，null表示无秒杀活动

	private BigDecimal couponDiscount;//商品券优惠金额(存数据库)
	private BigDecimal promotionDiscount;//活动优惠金额

	private BigDecimal originalAmount;//总价(原价)
	private BigDecimal finalAmount;//实付金额
}
