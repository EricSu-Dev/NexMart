package com.nex.nexmart.model.vo.order;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReturnOrderDetailVO {

	private Long id;
	/**
	 * 订单号
	 */
	private String orderNo;

	private Long orderId;


	/**
	 * 订单项ID
	 */
	private OrderItemVO orderItemVO;

	/**
	 * 退货原因
	 */
	private String reason;

	/**
	 * 0申请中 1已批准 2已拒绝 3退款处理中 4已退款 5用户取消
	 */
	private Integer status;

	/**
	 * 期望退款金额
	 */
	private BigDecimal expectedRefundAmount;

	/**
	 * 实际退款金额
	 */
	@TableField("actual_refund_amount")
	private BigDecimal actualRefundAmount;

	/**
	 * 拒绝原因
	 */
	private String rejectReason;

	/**
	 *
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	//申请图片
	@TableField("images")
	private String images;

}
