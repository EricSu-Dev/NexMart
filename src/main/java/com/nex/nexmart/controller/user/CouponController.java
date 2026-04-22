package com.nex.nexmart.controller.user;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.coupon.AvailableCouponVO;
import com.nex.nexmart.model.vo.coupon.CouponListVO;
import com.nex.nexmart.model.vo.coupon.MyCouponVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.coupon.CouponService;
import com.nex.nexmart.service.intf.coupon.CouponUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name="用户端-优惠券接口")
@RestController("UserCouponController")
@RequestMapping("/api/user/coupon")
@RequiredArgsConstructor
public class CouponController {

	private final CouponService couponService;
	private final CouponUserService couponUserService;

	@GetMapping("/list")
	@Operation(summary = "获取可领券列表")
	public Result<List<CouponListVO>> list(@RequestParam(required = false) Integer discountType, @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails == null ? null : userDetails.getUser().getId();
		log.info("获取可领券列表: userId={}", userId);
		return Result.success(couponService.getAvailableCoupons(userId, discountType));
	}

	@PostMapping("/{id}/receive")
	@Operation(summary = "领取优惠券")
	public Result<Void> receive(@PathVariable Long id,
	                            @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("领取优惠券: userId={}, couponId={}", userId, id);
		couponUserService.receiveCoupon(id, userId);
		return Result.success();
	}

	@GetMapping("/my")
	@Operation(summary = "查询我的券包")
	public Result<List<MyCouponVO>> my(@RequestParam(required = false) Integer status,
	                                   @RequestParam(required = false) Integer couponType,
	                                   @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("查询我的券包: userId={}, status={}, couponType={}", userId, status, couponType);
		return Result.success(couponUserService.getMyCoupons(userId, status, couponType));
	}

	@GetMapping("/available/productCoupon")
	@Operation(summary = "结算界面查询可用商品券列表")
	public Result<List<AvailableCouponVO>> availableProductCoupons(
			@RequestParam Long cartItemId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("查询可用商品券列表: userId={}, cartItemId={}", userId, cartItemId);
		return Result.success(couponUserService.getAvailableProductCoupons(userId, cartItemId));
	}

	@GetMapping("/available/orderCoupon")
	@Operation(summary = "结算界面查询可用订单券列表")
	public Result<List<AvailableCouponVO>> availableOrderCoupons(
			@RequestParam List<Long> cartItemIds,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("查询可用订单券列表: userId={}, cartItemIds={}", userId, cartItemIds);
		return Result.success(couponUserService.getAvailableOrderCoupons(userId, cartItemIds));
	}
}
