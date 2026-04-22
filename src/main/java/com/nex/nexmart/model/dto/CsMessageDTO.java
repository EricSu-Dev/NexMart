package com.nex.nexmart.model.dto;

import lombok.Data;

@Data
public class CsMessageDTO {
	private String action;    // MESSAGE / READ_ACK ,READ_ACK用于判断是否已读
	private Long sessionId;   // 所属会话ID
	private Integer type;          // 1文字 2图片 3商品卡片 4订单卡片
	private String content;   // 消息内容
	private String images;         // 图片URLs
	private Long productId;        // 商品ID
	private Long orderId;          // 订单ID
}