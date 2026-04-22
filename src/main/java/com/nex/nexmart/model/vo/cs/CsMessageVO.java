package com.nex.nexmart.model.vo.cs;

import com.nex.nexmart.model.vo.order.OrderVO;
import com.nex.nexmart.model.vo.product.ProductVO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CsMessageVO {
	private Long id;
	private Long sessionId;
	private Integer senderType;  // 1用户 2管理员
	private Long senderId;
	private String content;
	private Integer isRead;
	private LocalDateTime createdAt;
	private Integer type;// 1文本 2图片 3商品 4订单
	private List<String> imageList;
	// 商品卡片信息
	private CsProductCardVO productCard;   // 包含商品名/图片/价格等
	// 订单卡片信息
	private CsOrderCardVO orderCard;       // 包含订单号/金额/状态等
}

