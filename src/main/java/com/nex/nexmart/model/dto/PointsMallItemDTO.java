package com.nex.nexmart.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsMallItemDTO {
	@NotNull(message = "券ID不能为空")
	private Long couponId;

	@NotNull(message = "所需积分不能为空")
	@Min(value = 1, message = "所需积分至少为1")
	private Integer pointsCost;

}
