package com.nex.nexmart.model.dto.coupon;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponStatusDTO {
	@NotNull
	private Integer status;
}
