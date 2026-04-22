package com.nex.nexmart.model.entity.coupon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户持有优惠券表
 * @TableName coupon_user
 */
@TableName(value ="coupon_user")
@Data
public class CouponUser implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 关联 coupon
     */
    private Long couponId;

	/**
     * 优惠券类型: 1=单一商品券 2=秒杀订单券
     */
	private Integer couponType;

    /**
     * 状态: 0=未使用 1=已使用 2=已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime receivedAt;

    /**
     * 到期时间(领取时算好写入)
     */
    private LocalDateTime expireAt;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    /**
     * 使用时关联的订单ID
     */
    private Long orderId;

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