package com.nex.nexmart.service.intf.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.order.OrderCreateDTO;
import com.nex.nexmart.model.dto.order.OrderPreviewDTO;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.vo.order.OrderPreviewVO;
import com.nex.nexmart.model.vo.order.OrderVO;
import jakarta.validation.Valid;

/**
 * 订单服务
 */
public interface OrderService extends IService<Order> {

	String createOrder(@Valid OrderCreateDTO dto, Long userId);

	PageResult<OrderVO> OrdersPage(long current, long size, Long userId, Integer status,String keyword);

    OrderVO orderDetail(Long id, Long userId);

    void cancelOrder(Long id, Long userId);

    void confirmReceipt(Long id, Long userId);

	void rebuy(Long id, Long userId);

	void updateStatus(Long id, Integer status);

	String getStatusDesc(Integer status);

	OrderPreviewVO preview(Long userId, OrderPreviewDTO dto);

}
