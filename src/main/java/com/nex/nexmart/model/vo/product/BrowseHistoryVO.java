package com.nex.nexmart.model.vo.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BrowseHistoryVO {
	private Long id;
	private Long productId;
	private String name;
	private String coverUrl;
	private BigDecimal price;
	private Integer stock;
	private LocalDateTime viewedAt;

	private String promotionName;
	private BigDecimal discountedPrice;

	private Long categoryId;  // 内存匹配促销用，前端不需要展示
}
