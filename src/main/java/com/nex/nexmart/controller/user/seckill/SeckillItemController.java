package com.nex.nexmart.controller.user.seckill;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.seckill.SeckillCouponItemVO;
import com.nex.nexmart.model.vo.seckill.SeckillProductItemVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "用户端-秒杀项")
@RestController("UserSeckillItemController")
@RequestMapping("/api/user/seckill/item")
@RequiredArgsConstructor
public class SeckillItemController {

	private final SeckillItemService seckillItemService;

	@GetMapping("/product/list")
	@Operation(summary = "获取活动商品秒杀项列表")
	public Result<List<SeckillProductItemVO>> productList(
			@RequestParam Long activityId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("获取活动商品秒杀项列表: {}", activityId);
		return Result.success(seckillItemService.productListByActivity(activityId, userId));
	}

	@GetMapping("/coupon/list")
	@Operation(summary = "获取活动券秒杀项列表")
	public Result<List<SeckillCouponItemVO>> couponList(
			@RequestParam Long activityId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("获取活动券秒杀项列表: {}", activityId);
		return Result.success(seckillItemService.couponListByActivity(activityId, userId));
	}
}
