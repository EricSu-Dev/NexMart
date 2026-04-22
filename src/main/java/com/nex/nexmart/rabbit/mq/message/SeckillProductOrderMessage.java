package com.nex.nexmart.rabbit.mq.message;

import lombok.Data;

@Data
public class SeckillProductOrderMessage {
	private Long userId;
	private Long seckillItemId;
	private Long skuId;
	private Long addressId;
}
