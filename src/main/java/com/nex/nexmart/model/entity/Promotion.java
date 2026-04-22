package com.nex.nexmart.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 限时优惠活动表
 * @TableName promotion
 */
@TableName(value ="promotion")
@Data
public class Promotion implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 优惠类型 1=满减 2=折扣
     */
    private Integer type;

    /**
     * 满减金额（type=1）
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率 如0.9=九折（type=2）
     */
    private BigDecimal discountRate;

    /**
     * 最低消费金额，0=无门槛
     */
    private BigDecimal minAmount;

    /**
     * 范围 1=全场 2=分类 3=单商品
     */
    private Integer scope;

    /**
     * scope=2时category_id，scope=3时product_id,1代表全场
     */
    private Long scopeId;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 1=启用 0=禁用
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime createdAt;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}