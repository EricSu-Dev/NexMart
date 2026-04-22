package com.nex.nexmart.controller.user.order;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.order.ReturnApplyDTO;
import com.nex.nexmart.model.vo.order.ReturnOrderDetailVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-申请退款接口")
@RestController("userReturnOrderController")
@RequestMapping("/api/user/refund")
@RequiredArgsConstructor
@Slf4j
public class ReturnOrderController {
	private final ReturnOrderService returnOrderService;
	/**
	 * 用户申请退货
	 */
	@PostMapping("/{id}/apply")
	@Operation(summary = "用户申请退货")
	public Result<Void> apply(@PathVariable Long id,
	                          @RequestBody ReturnApplyDTO dto,
	                          @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order apply id={} orderItemId={} userId={} reason={}", id, dto.getOrderItemId(), userId, dto.getReason());
		returnOrderService.apply(id, dto,userId);
		return Result.success();
	}

	@Operation(summary = "取消申请退货")
	@PutMapping("/{id}/cancelApply")
	public Result<Void> cancelApply(@PathVariable Long id,
	                                @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order cancel id={} userId={}", id, userId);
		returnOrderService.cancelApply(id, userId);
		return Result.success();
	}
	@GetMapping("/detail/{id}")
	@Operation(summary = "退货详情")
	public Result<ReturnOrderDetailVO> getDetail(@PathVariable Long id,
	                                             @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Order detail id={}", id);
		return Result.success(returnOrderService.detail(id, userId));
	}
}
