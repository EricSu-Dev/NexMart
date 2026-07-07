package com.nex.nexmart.model.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderPreviewDTO {
	@NotEmpty(message = "请选择商品")
	private List<@NotNull(message = "购物车条目ID不能为空") Long> cartItemIds;
	private Long orderUserCouponId;              // 秒杀订单券，可为null
	private Map<Long, Long> productCouponMap;    // key=cartItemId, value=userCouponId，可为null
}
