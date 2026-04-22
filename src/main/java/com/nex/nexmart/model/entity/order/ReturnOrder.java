package com.nex.nexmart.model.entity.order;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @TableName return_order
 */
@TableName(value ="return_order")
@Data
@Builder
public class ReturnOrder implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

	/**
	 * 订单项ID
	 */
	private Long orderItemId;

    /**
     * 退货原因
     */
    private String reason;

    /**
     * 0申请中 1已批准 2已拒绝 3退款处理中 4已退款
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

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

	@TableField("images")
	private String images;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}