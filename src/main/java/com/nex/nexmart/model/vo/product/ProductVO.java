package com.nex.nexmart.model.vo.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {

    private Long id;

    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String coverUrl;
    private Integer status;
    private LocalDateTime createdAt;
	private Integer hasSpec;
	private Integer sales;
	private List<ProductSpecVO>  productSpecList;

	private String promotionName;       // 活动名，null表示无活动
	private BigDecimal discountedPrice; // 折后价，null表示无活动

	private Long itemId; // 手动模式配置项其特有的 ID
}
