package com.nex.nexmart.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionVO {
	private Long id;
	private String name;
	private Integer type;
	private BigDecimal discountAmount;
	private BigDecimal discountRate;
	private BigDecimal minAmount;
	private Integer scope;
	private Long scopeId;
	private String scopeName;       // 分类名或商品名，scope=1时为"全场"
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer status;
	private LocalDateTime createdAt;
	private Integer stage;//表示活动阶段
}
