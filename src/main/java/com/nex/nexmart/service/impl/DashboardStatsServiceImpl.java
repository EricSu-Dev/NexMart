package com.nex.nexmart.service.impl;

import com.nex.nexmart.common.constant.OrderStatusConstants;
import com.nex.nexmart.common.constant.ReturnOrderStatusConstant;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.ReturnOrder;
import com.nex.nexmart.model.vo.dashboard.DailyOrderStatsVO;
import com.nex.nexmart.model.vo.dashboard.DailyRevenueStatsVO;
import com.nex.nexmart.model.vo.dashboard.DashboardStatsVO;
import com.nex.nexmart.service.intf.DashboardStatsService;
import com.nex.nexmart.service.intf.UserService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private ReturnOrderService returnOrderService;

	@Autowired
	private OrderMapper orderMapper;
	@Override
	public DashboardStatsVO stats() {
		DashboardStatsVO vo = new DashboardStatsVO();
		vo.setTotalProducts(productService.count());
		vo.setTotalOrders(orderService.count());
		Long userCount = userService.lambdaQuery().eq(User::getRole, UserRoleConstants.ROLE_USER).count();
		vo.setTotalUsers(userCount);
		Long pendingDeliveryCount = orderService.lambdaQuery().eq(Order::getStatus, OrderStatusConstants.PENDING_DELIVERY).count();
		vo.setPendingDelivery(pendingDeliveryCount);
		Long refundingOrderCount = returnOrderService
				.lambdaQuery()
				.in(ReturnOrder::getStatus, ReturnOrderStatusConstant.APPLYING, ReturnOrderStatusConstant.APPROVED, ReturnOrderStatusConstant.REFUND_PROCESSING)
				.count();
		vo.setRefundingOrders(refundingOrderCount);
		return vo;
	}

	public List<DailyOrderStatsVO> getDailyOrderStats(int days) {
		return orderMapper.getDailyOrderStats(days);
	}

	public List<DailyRevenueStatsVO> getDailyRevenueStats(int days) {
		return orderMapper.getDailyRevenueStats(days);
	}
}
