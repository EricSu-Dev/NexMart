package com.nex.nexmart.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 支付宝交易流水号 */
    private String payNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式: 1=支付宝 */
    private Integer payType;

    /** 0=未支付 1=已支付 2=已退款 */
    private Integer status;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
