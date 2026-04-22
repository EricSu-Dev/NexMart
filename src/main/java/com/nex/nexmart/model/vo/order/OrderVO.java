package com.nex.nexmart.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Integer status;
    private String statusDesc;
    /** 支付状态: 0=未支付 1=已支付 */
    private Integer payStatus;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String remark;
    private LocalDateTime createdAt;
	private LocalDateTime completeTime;
    private List<OrderItemVO> items;
	private Integer totalQuantity;//订单商品总数

	private BigDecimal originalAmount;//原价
	private BigDecimal finalAmount;//实付金额,

	private String orderCouponName; //订单券名称

	private BigDecimal promotionTotalDiscount;//活动总计优惠金额
	private BigDecimal productCouponTotalDiscount;//商品券总计优惠金额
	private BigDecimal orderCouponDiscount;//订单券优惠金额
	private BigDecimal seckillDiscount;//秒杀优惠金额

}
