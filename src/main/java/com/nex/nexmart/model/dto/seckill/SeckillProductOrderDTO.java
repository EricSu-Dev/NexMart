package com.nex.nexmart.model.dto.seckill;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillProductOrderDTO {
	@NotNull
	private Long seckillItemId;
	@NotNull
	private Long addressId;
}
