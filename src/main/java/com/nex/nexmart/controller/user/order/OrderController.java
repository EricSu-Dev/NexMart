package com.nex.nexmart.controller.user.order;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.order.OrderCreateDTO;
import com.nex.nexmart.model.dto.order.OrderPreviewDTO;
import com.nex.nexmart.model.vo.order.OrderPreviewVO;
import com.nex.nexmart.model.vo.order.OrderVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-订单接口")
@RestController("userOrderController")
@RequestMapping("/api/user/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "提交订单")
	@PostMapping("/create")
	public Result<String> create(@Valid @RequestBody OrderCreateDTO dto,
	                             @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order create userId={} cartItemCount={}", userId, dto.getCartItemIds().size());
		String orderNo = orderService.createOrder(dto, userId);
		return Result.success("下单成功", orderNo);
	}

	@Operation(summary = "查询我的订单列表")
	@GetMapping("/my")
	public Result<PageResult<OrderVO>> myOrders(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) String keyword,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {

		Long userId = userDetails.getUser().getId();
		log.info("Order my list userId={} current={} size={} status={}", userId, current, size, status);
		return Result.success(orderService.OrdersPage(current, size, userId, status,keyword));
	}

	@Operation(summary = "订单详情")
	@GetMapping("/{id}")
	public Result<OrderVO> detail(@PathVariable Long id,
	                              @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order detail id={} userId={}", id, userId);
		return Result.success(orderService.orderDetail(id, userId));
	}

	@Operation(summary = "取消订单")
	@PutMapping("/{id}/cancel")
	public Result<Void> cancel(@PathVariable Long id,
	                           @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order cancel id={} userId={}", id, userId);
		orderService.cancelOrder(id, userId);
		return Result.success();
	}

	@Operation(summary = "确认收货")
	@PutMapping("/{id}/confirm")
	public Result<Void> confirm(@PathVariable Long id,
	                            @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("确认收货 id={} userId={}", id, userId);
		orderService.confirmReceipt(id, userId);
		return Result.success();
	}


	@PostMapping("/{id}/rebuy")
	@Operation(summary = "再次购买")
	public Result<Void> rebuy(@PathVariable Long id,@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("再次购买 id={} userId={}", id, userId);
		orderService.rebuy(id, userId);
		return Result.success();
	}

	@PostMapping("/preview")
	@Operation(summary = "结算界面订单价格预览")
	public Result<OrderPreviewVO> preview(
			@RequestBody @Validated OrderPreviewDTO dto,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("订单价格预览: userId={}", userId);
		return Result.success(orderService.preview(userId, dto));
	}

}
