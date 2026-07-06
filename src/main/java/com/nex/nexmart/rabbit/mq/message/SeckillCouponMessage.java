package com.nex.nexmart.rabbit.mq.message;

import lombok.Data;

@Data
public class SeckillCouponMessage {
	private String messageId;
	private Long userId;
	private Long seckillItemId;
}
