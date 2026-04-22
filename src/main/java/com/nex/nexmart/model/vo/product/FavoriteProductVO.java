package com.nex.nexmart.model.vo.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteProductVO {
	private Long id;           // 收藏记录ID
	private Long productId;
	private String name;
	private String coverUrl;
	private BigDecimal price;
	private Integer stock;
	private LocalDateTime createdAt;  // 收藏时间

	private String promotionName;
	private BigDecimal discountedPrice;

	private Long categoryId;  // 内存匹配促销用，前端不需要展示
}

