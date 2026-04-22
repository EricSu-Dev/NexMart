package com.nex.nexmart.model.dto.seckill;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddSeckillItemDTO {
	@NotNull
	private Integer itemType;

	// itemType=1 时必填
	private Long productId;
	private Long productSpecId; //商品无规格为null
	private BigDecimal seckillPrice;
	private Integer seckillStock;
	private Integer perLimit;

	// itemType=2 时必填
	private Long couponId;

}
