package com.nex.nexmart.rabbit.mq.message;

import lombok.Data;

@Data
public class SeckillCouponMessage {
	private Long userId;
	private Long seckillItemId;
}
