package com.nex.nexmart.model.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnApplyDTO {
	private Long orderItemId;
	private String reason;
	private String images;
	private BigDecimal expectedRefundAmount;
}