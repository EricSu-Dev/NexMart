package com.nex.nexmart.service.impl.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.constant.OrderStatusConstants;
import com.nex.nexmart.common.constant.ReturnOrderStatusConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.model.dto.order.ReturnApplyDTO;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.order.ReturnOrder;
import com.nex.nexmart.model.vo.order.OrderItemVO;
import com.nex.nexmart.model.vo.order.ReturnOrderDetailVO;
import com.nex.nexmart.model.vo.order.ReturnOrderVO;
import com.nex.nexmart.mapper.base.ReturnOrderMapper;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.PaymentService;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import com.nex.nexmart.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
* @author Eric
*  针对表【return_order】的数据库操作Service实现
*  2026-03-31 15:56:05
*/
@Service
@RequiredArgsConstructor
public class ReturnOrderServiceImpl extends ServiceImpl<ReturnOrderMapper, ReturnOrder> implements ReturnOrderService {

	private  final PaymentService paymentService;
	private final OrderItemService orderItemService;
	private final OrderMapper orderMapper;
	private final WebSocketSessionManager sessionManager;
	@Override
	public void apply(Long id, ReturnApplyDTO dto, Long userId) {
		Order order = orderMapper.selectById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		if (order.getStatus() != OrderStatusConstants.COMPLETED) {
			throw new BusinessException("只有已完成的订单可以申请退货");
		}
		OrderItem orderItem = orderItemService.getById(dto.getOrderItemId());
		if (orderItem == null || !orderItem.getOrderId().equals(id)) {
			throw new BusinessException("订单项不存在");
		}
		// 校验是否在7天退货期限内
		LocalDateTime deadline = order.getCompleteTime().plusDays(7);
		if (order.getCompleteTime() == null) {
			throw new BusinessException("订单完成时间异常，请联系客服");
		}
		if (LocalDateTime.now().isAfter(deadline)) {
			throw new BusinessException("已超过退货期限(7天)");
		}
		// 校验该订单项是否已申请过退货
		long count = lambdaQuery()
				.eq(ReturnOrder::getOrderItemId, dto.getOrderItemId())
				.count();
		if (count > 0) {
			throw new BusinessException("该商品已申请过退货");
		}

		if (dto.getExpectedRefundAmount().compareTo(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))) > 0) {
			throw new BusinessException("退款金额不能大于商品总价");
		}

		ReturnOrder returnOrder = ReturnOrder.builder()
				.orderId(order.getId())
				.orderItemId(dto.getOrderItemId())
				.userId(userId)
				.reason(dto.getReason())
				.images(dto.getImages())
				.status(ReturnOrderStatusConstant.APPLYING)
				.expectedRefundAmount(dto.getExpectedRefundAmount())
				.build();
		save(returnOrder);
		// 用户申请退款时
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String message = JSON.toJSONString(Map.of(
				"type", "REFUND_APPLY",
				"orderId", order.getId(),
				"amount", dto.getExpectedRefundAmount(),
				"time", LocalDateTime.now().format(formatter)
		));
		sessionManager.broadcast(message);
	}

	@Override
	public void cancelApply(Long id, Long userId) {
		ReturnOrder returnOrder = getById(id);
		if (returnOrder == null || !returnOrder.getUserId().equals(userId)) {
			throw new BusinessException("退货订单不存在");
		}
		if (returnOrder.getStatus() != ReturnOrderStatusConstant.APPLYING
				&& returnOrder.getStatus() != ReturnOrderStatusConstant.APPROVED) {
			throw new BusinessException("退货订单状态错误");
		}
		returnOrder.setStatus(ReturnOrderStatusConstant.CANCELED);
		updateById(returnOrder);
		// 发送消息给管理员
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String message = JSON.toJSONString(Map.of(
				"type", "CANCEL_REFUND_APPLY",
				"orderId", returnOrder.getOrderId(),
				"amount", returnOrder.getExpectedRefundAmount(),
				"time", LocalDateTime.now().format(formatter)
		));
		sessionManager.broadcast(message);
	}

	@Override
	public ReturnOrderDetailVO detail(Long id, Long userId) {
		ReturnOrder returnOrder = getById(id);
		if (returnOrder == null || !returnOrder.getUserId().equals(userId)) {
			throw new BusinessException("退货订单不存在");
		}
		ReturnOrderDetailVO vo = new ReturnOrderDetailVO();
		BeanUtils.copyProperties(returnOrder,vo);
		String orderNo = orderMapper.selectById(returnOrder.getOrderId()).getOrderNo();
		if(orderNo == null){
			throw new BusinessException("订单号异常");
		}
		vo.setOrderNo(orderNo);
		OrderItem item = orderItemService.getById(returnOrder.getOrderItemId());
		if(item == null){
			throw new BusinessException("订单项不存在");
		}
		OrderItemVO itemVO = new OrderItemVO();
		BeanUtils.copyProperties(item,itemVO);
		BigDecimal originalAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
		BigDecimal promotionDiscount = item.getPromotionalPrice() != null
				? originalAmount.subtract(item.getPromotionalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				: BigDecimal.ZERO;
		BigDecimal coupon = Optional.ofNullable(item.getCouponDiscount()).orElse(BigDecimal.ZERO);
		itemVO.setOriginalAmount(originalAmount);
		itemVO.setPromotionDiscount(promotionDiscount);
		itemVO.setFinalAmount(originalAmount.subtract(promotionDiscount).subtract(coupon));
		vo.setOrderItemVO(itemVO);
		return vo;
	}

	//==============================================管理端======================================
	@Override
	public PageResult<ReturnOrderVO> returnOrderList(long current, long size, Integer status) {
		IPage<ReturnOrderVO> VOPage = lambdaQuery()
				.eq(status != null, ReturnOrder::getStatus, status)
				.orderByDesc(ReturnOrder::getCreatedAt)
				.page(new Page<>(current, size))
				.convert(r -> {
					ReturnOrderVO vo = new ReturnOrderVO();
					BeanUtils.copyProperties(r, vo);
					vo.setStatusDesc(getReturnStatusDesc(r.getStatus()));
					vo.setImages(r.getImages() != null
							? Arrays.asList(r.getImages().split(","))
							: null);
					OrderItem item = orderItemService.getById(r.getOrderItemId());
					if(item == null){
						throw new BusinessException("订单项不存在");
					}
					vo.setProductName(item.getProductName());
					vo.setQuantity(item.getQuantity());
					Order order = orderMapper.selectById(item.getOrderId());
					if(order == null){
						throw new BusinessException("订单不存在");
					}
					vo.setOrderNo(order.getOrderNo());
					return vo;
				});
		return PageResult.of(VOPage);
	}

	@Override
	public ReturnOrderDetailVO adminDetail(Long id) {
		ReturnOrder returnOrder = getById(id);
		if (returnOrder == null ) {
			throw new BusinessException("退货订单不存在");
		}
		ReturnOrderDetailVO vo = new ReturnOrderDetailVO();
		BeanUtils.copyProperties(returnOrder,vo);
		String orderNo = orderMapper.selectById(returnOrder.getOrderId()).getOrderNo();
		if(orderNo == null){
			throw new BusinessException("订单号异常");
		}
		vo.setOrderNo(orderNo);
		OrderItem item = orderItemService.getById(returnOrder.getOrderItemId());
		if(item == null){
			throw new BusinessException("订单项不存在");
		}
		OrderItemVO itemVO = new OrderItemVO();
		BeanUtils.copyProperties(item,itemVO);
		BigDecimal originalAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
		BigDecimal promotionDiscount = item.getPromotionalPrice() != null
				? originalAmount.subtract(item.getPromotionalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				: BigDecimal.ZERO;
		BigDecimal coupon = Optional.ofNullable(item.getCouponDiscount()).orElse(BigDecimal.ZERO);
		itemVO.setOriginalAmount(originalAmount);
		itemVO.setPromotionDiscount(promotionDiscount);
		itemVO.setFinalAmount(originalAmount.subtract(promotionDiscount).subtract(coupon));
		vo.setOrderItemVO(itemVO);
		return vo;
	}

	private String getReturnStatusDesc(Integer status) {
		switch (status) {
			case ReturnOrderStatusConstant.APPLYING:  return "退货申请中";
			case ReturnOrderStatusConstant.APPROVED:  return "退货已批准";
			case ReturnOrderStatusConstant.REJECTED:  return "退货已拒绝";
			case ReturnOrderStatusConstant.REFUND_PROCESSING: return "退款处理中";
			case ReturnOrderStatusConstant.REFUNDED:  return "退款已完成";
			case ReturnOrderStatusConstant.CANCELED:  return "用户已取消";
			default: return "未知状态";
		}
	}

	//拒绝时拒绝原因应该必填，批准时实际退款金额应该必填，并且要校验金额不能超过原退款金额
	@Override
	public void audit(Long returnId, Integer status, String rejectReason, BigDecimal actualRefundAmount) {
		ReturnOrder one = getById(returnId);
		if (one == null) {
			throw new BusinessException("退货订单不存在");
		}
		if (one.getStatus() != ReturnOrderStatusConstant.APPLYING) {
			throw new BusinessException("退货订单状态错误");
		}
		if (status == ReturnOrderStatusConstant.REJECTED) {
			if (rejectReason == null || rejectReason.isEmpty()) {
				throw new BusinessException("请填写拒绝原因");
			}
			one.setRejectReason(rejectReason);
		}
		if (status == ReturnOrderStatusConstant.APPROVED) {
			if (actualRefundAmount == null) {
				throw new BusinessException("请填写实际退款金额");
			}
			if (actualRefundAmount.compareTo(one.getExpectedRefundAmount()) > 0) {
				throw new BusinessException("退款金额不能超过原退款金额");
			}
			one.setActualRefundAmount(actualRefundAmount);
		}
		one.setStatus(status);
		updateById(one); // 保存到数据库
	}

	@Override
	public void refund(Long returnId) {
		ReturnOrder one = getById(returnId);
		if (one == null) {
			throw new BusinessException("退货订单不存在");
		}
		if (one.getStatus() != ReturnOrderStatusConstant.APPROVED) {
			throw new BusinessException("退货订单状态错误");
		}
		one.setStatus(ReturnOrderStatusConstant.REFUND_PROCESSING);
		updateById(one);
		// 调用支付宝沙箱进行退款
		try {
			paymentService.refund(returnId);
		} catch (Exception e) {
			// 退款失败，回退状态让管理员可以重新操作退款
			one.setStatus(ReturnOrderStatusConstant.APPROVED);
			updateById(one);
			throw new BusinessException("退款失败：" + e.getMessage());
		}
	}
}




