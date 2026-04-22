package com.nex.nexmart.model.dto.seckill;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BindSeckillItemDTO {
	@NotNull
	private Long activityId;
	private List<Long> itemIds;
}
