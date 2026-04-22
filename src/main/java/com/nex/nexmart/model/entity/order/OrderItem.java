package com.nex.nexmart.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细表
 *  order_item
 */
@TableName(value ="order_item")
@Data
public class OrderItem {
    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID（冗余，商品删除后仍可查）
     */
    private Long productId;

    /**
     * 下单时商品名称（冗余快照）
     */
    private String productName;

    /**
     * 下单时商品封面（冗余快照）
     */
    private String coverUrl;

    /**
     * 购买数量
     */
    private Integer quantity;

	private String specName;


	private BigDecimal price;//下单时单价
	private BigDecimal promotionalPrice;//活动优惠后单价
	private BigDecimal couponDiscount;//商品券优惠金额
	private BigDecimal seckillPrice; //秒杀优惠后单价

	private String promotionName;//活动名，null表示无活动
	private String couponName;//商品券名，null表示没用券
	private String seckillName;//秒杀活动名，null表示无秒杀活动


}