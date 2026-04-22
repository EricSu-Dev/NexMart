package com.nex.nexmart.model.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 秒杀商品表
 * seckill_item
 */
@TableName(value = "seckill_item")
@Data
public class SeckillItem implements Serializable {
	/**
	 *
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 关联活动
	 */
	private Long activityId;

	/**
	 * 1=商品 2=优惠券
	 */
	private Integer itemType;

	/**
	 * 关联商品(item_type=1)
	 */
	private Long productId;

	private Long productSpecId;

	/**
	 * 秒杀价(item_type=1)
	 */
	private BigDecimal seckillPrice;

	/**
	 * 关联优惠券(item_type=2)
	 */
	private Long couponId;

	/**
	 * 秒杀库存
	 */
	private Integer seckillStock;

	/**
	 * 已售数量
	 */
	private Integer soldCount;

	/**
	 * 每人限购数
	 */
	private Integer perLimit;

	/**
	 * 1=上架 2=下架
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