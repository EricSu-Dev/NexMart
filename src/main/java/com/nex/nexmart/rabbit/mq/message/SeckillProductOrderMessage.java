package com.nex.nexmart.rabbit.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillProductOrderMessage {
	private String messageId;
	private Long userId;
	private Long seckillItemId;
	private Long skuId;
	private Long addressId;
}
