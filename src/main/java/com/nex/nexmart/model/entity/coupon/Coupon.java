package com.nex.nexmart.model.entity.coupon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 优惠券模板表
 * @TableName coupon
 */
@TableName(value ="coupon")
@Data
public class Coupon implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 券名称
     */
    private String name;

    /**
     * 券类型: 1=普通商品券 2=秒杀订单券
     */
    private Integer couponType;

    /**
     * 优惠方式: 1=满减 2=折扣 3=无门槛
     */
    private Integer discountType;

    /**
     * 满减门槛金额(满减券用)
     */
    private BigDecimal minAmount;

    /**
     * 减免金额(满减/无门槛用)
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率 0.00~1.00(折扣券用)
     */
    private BigDecimal discountRate;

    /**
     * 适用范围: 1=全场 2=单分类 3=单商品
     */
    private Integer scope;

    /**
     * 分类ID或商品ID
     */
    private Long scopeId;

    /**
     * 发放总量, -1=不限量
     */
    private Integer total;

    /**
     * 剩余可领数量
     */
    private Integer remained;

    /**
     * 每人限领张数
     */
    private Integer perLimit;

    /**
     * 领取开始时间
     */
    private LocalDateTime receiveStart;

    /**
     * 领取截止时间
     */
    private LocalDateTime receiveEnd;

	/**
	 * 领取渠道: 1=领券中心 2=积分商城 3=秒杀 null=暂无
	 */
    private Integer receiveChannel;

    /**
     * 领取后N天内有效
     */
    private Integer validDays;

    /**
     * 状态: 1=上架 0=下架
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