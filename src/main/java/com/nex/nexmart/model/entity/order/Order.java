package com.nex.nexmart.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data

@TableName(value = "`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

	private String orderNo;

    private Long userId;

    /** 关联的地址ID（用于溯源） */
    private Long addressId;

	/**
	 * 订单流转状态: 0=取消 1=待付款 2=待发货 3=待收货 4=完成
	 */
	private Integer status;

	/**
	 * 支付状态: 0=未支付 1=已支付
	 * 与 status 分开，因为取消后仍需保留支付状态记录
	 */
	private Integer payStatus;

    private BigDecimal originalAmount;//原价
	private BigDecimal finalAmount;//实付金额

	private String orderCouponName; //订单券名称

	private BigDecimal promotionTotalDiscount;//活动总计优惠金额
	private BigDecimal productCouponTotalDiscount;//商品券总计优惠金额
	private BigDecimal orderCouponDiscount;//订单券优惠金额


    private String receiverName;

    private String receiverPhone;

    private String address;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

	@TableField("complete_time")
	private LocalDateTime completeTime;

	private Long seckillItemId;

	private BigDecimal seckillDiscount;


}
