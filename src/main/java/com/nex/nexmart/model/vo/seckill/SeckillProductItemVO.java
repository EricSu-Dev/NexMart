package com.nex.nexmart.model.vo.seckill;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeckillProductItemVO {
	private Long id;
	private Integer itemType;

	// 商品信息
	private Long productId;
	private String productName;
	private String productImage;
	private BigDecimal originalPrice;
	private BigDecimal seckillPrice;
	private String specName;

	// 通用
	private Integer seckillStock;
	private Integer soldCount;
	private Integer perLimit;
	private Integer status;
	private String activityName;
	private Boolean purchased;
}
