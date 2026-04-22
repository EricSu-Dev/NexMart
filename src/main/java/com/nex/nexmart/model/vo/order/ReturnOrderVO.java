package com.nex.nexmart.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReturnOrderVO {
	private Long id;
	private Integer status;
	private String statusDesc;          // 状态描述
	private String reason;              // 退货原因
	private BigDecimal expectedRefundAmount; // 期望退款金额
	private BigDecimal actualRefundAmount;// 实际退款金额
	private String rejectReason;        // 拒绝原因
	private List<String> images;        // 图片列表
	private LocalDateTime createdAt;   /// 创建时间
	private String productName;         // 商品名称
	private Integer quantity;          // 退货数量
	private String orderNo;             // 订单编号
}
