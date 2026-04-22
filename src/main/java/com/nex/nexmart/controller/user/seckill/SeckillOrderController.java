package com.nex.nexmart.controller.user.seckill;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.seckill.SeckillProductOrderDTO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.seckill.SeckillOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端-秒杀订单")
@Slf4j
@RestController
@RequestMapping("/api/user/seckill/order")
@RequiredArgsConstructor
public class SeckillOrderController {

	private final SeckillOrderService seckillOrderService;

	@PostMapping("/coupon")
	@Operation(summary = "创建秒杀订单券订单")
	public Result<Void> createCouponOrder(Long seckillItemId,
	                                      @AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("创建秒杀订单券订单");
		Long userId = userDetails.getUser().getId();
		seckillOrderService.createCouponOrder(userId, seckillItemId);
		return Result.success();
	}

	@PostMapping("/product")
	@Operation(summary = "创建秒杀商品订单")
	public Result<Void> createProductOrder(
			@AuthenticationPrincipal SecurityUserDetails userDetails,
			@RequestBody @Validated SeckillProductOrderDTO dto) {
		log.info("创建秒杀商品订单");
		Long userId = userDetails.getUser().getId();
		seckillOrderService.createProductOrder(userId, dto);
		return Result.success();
	}
}
