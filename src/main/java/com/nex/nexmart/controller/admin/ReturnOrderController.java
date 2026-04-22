package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.order.ReturnOrderDetailVO;
import com.nex.nexmart.model.vo.order.ReturnOrderVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController("adminReturnOrderController")
@RequestMapping("/api/admin/return")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Tag(name = "管理端-退货订单接口")
public class ReturnOrderController {
	private final ReturnOrderService returnOrderService;
	/**
	 * 管理员审核退货（批准/拒绝）
	 */
	@PutMapping("/audit/{returnId}")
	@Operation(summary = "管理员审核退货")
	public Result<Void> audit(@PathVariable Long returnId,
	                       @RequestParam Integer status,
	                       @RequestParam(required = false) String rejectReason,
							@RequestParam(required = false) BigDecimal actualRefundAmount) {
		returnOrderService.audit(returnId, status, rejectReason, actualRefundAmount);
		return Result.success();
	}

	/**
	 * 管理员确认收到退货后发起退款
	 */
	@PostMapping("/refund/{returnId}")
	@Operation(summary = "管理员确认收到退货后发起退款")
	public Result<?> refund(@PathVariable Long returnId) {
		returnOrderService.refund(returnId);
		return Result.success();
	}

	/**
	 * 查看退货申请列表
	 */
	@GetMapping("/list")
	@Operation(summary = "查看退货申请列表")
	public Result<PageResult<ReturnOrderVO>> list(@RequestParam(defaultValue = "1") long current,
	                                              @RequestParam(defaultValue = "10") long size,
	                                              @RequestParam(required = false) Integer status) {
		log.info("ReturnOrder list current={} size={} status={}", current, size, status);

		return Result.success(returnOrderService.returnOrderList(current, size, status));
	}

	@GetMapping("/detail/{id}")
	@Operation(summary = "管理员获取退货订单详情")
	public Result<ReturnOrderDetailVO> getDetail(@PathVariable Long id)
	{

		log.info("Order detail id={}", id);
		return Result.success(returnOrderService.adminDetail(id));
	}

}
