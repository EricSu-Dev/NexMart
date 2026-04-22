package com.nex.nexmart.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionDTO {
	@NotBlank
	private String name;

	@NotNull
	private Integer type;           // 1=满减 2=折扣

	private BigDecimal discountAmount;  // type=1 时必填

	private BigDecimal discountRate;    // type=2 时必填

	@NotNull
	private BigDecimal minAmount;

	@NotNull
	private Integer scope;          // 1=全场 2=分类 3=单商品

	private Long scopeId;           // scope=2或3时必填

	@NotNull
	private LocalDateTime startTime;

	@NotNull
	private LocalDateTime endTime;
}