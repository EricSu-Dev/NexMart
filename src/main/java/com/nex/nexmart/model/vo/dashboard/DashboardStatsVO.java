package com.nex.nexmart.model.vo.dashboard;

import lombok.Data;

@Data
public class DashboardStatsVO {
	private Long totalProducts;      // 商品总数
	private Long totalOrders;        // 订单总数
	private Long totalUsers;         // 用户总数
	private Long pendingDelivery;    // 待发货订单
	private Long refundingOrders;    // 售后订单
}
