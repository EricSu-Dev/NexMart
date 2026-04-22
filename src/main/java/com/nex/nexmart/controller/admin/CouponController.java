package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.coupon.CouponCreateDTO;
import com.nex.nexmart.model.dto.coupon.CouponPageQueryDTO;
import com.nex.nexmart.model.dto.coupon.CouponStatusDTO;
import com.nex.nexmart.model.dto.coupon.CouponUpdateDTO;
import com.nex.nexmart.model.vo.coupon.CouponStatsVO;
import com.nex.nexmart.model.vo.coupon.CouponVO;
import com.nex.nexmart.service.intf.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Tag(name = "管理端-优惠券管理")
@Slf4j
@RestController("AdminCouponController")
@RequestMapping("/api/admin/coupon")
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@RequiredArgsConstructor
public class CouponController {
	private final CouponService couponService;

	@PostMapping
	@Operation(summary = "创建优惠券")
	public Result<Void> create(@RequestBody @Validated CouponCreateDTO dto) {
		log.info("创建优惠券: {}", dto.getName());
		couponService.createCoupon(dto);
		return Result.success();
	}

	@GetMapping("/page")
	@Operation(summary = "分页查询优惠券列表")
	public Result<PageResult<CouponVO>> page(CouponPageQueryDTO dto) {
		log.info("分页查询优惠券列表: current={}, size={}", dto.getCurrent(), dto.getSize());
		return Result.success(couponService.pageCoupon(dto));
	}

	@PutMapping("/{id}")
	@Operation(summary = "修改优惠券")
	public Result<Void> update(@PathVariable Long id, @RequestBody @Validated CouponUpdateDTO dto) {
		log.info("修改优惠券: id={}", id);
		dto.setId(id);
		couponService.updateCoupon(dto);
		return Result.success();
	}

	@PutMapping("/{id}/status")
	@Operation(summary = "修改优惠券状态")
	public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Validated CouponStatusDTO dto) {
		log.info("修改优惠券状态: id={}, status={}", id, dto.getStatus());
		couponService.updateStatus(id, dto.getStatus());
		return Result.success();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "删除优惠券")
	public Result<Void> delete(@PathVariable Long id) {
		log.info("删除优惠券: id={}", id);
		couponService.deleteCoupon(id);
		return Result.success();
	}

	@GetMapping("/{id}/stats")
	@Operation(summary = "查询优惠券统计")
	public Result<CouponStatsVO> stats(@PathVariable Long id) {
		log.info("查询优惠券统计: id={}", id);
		return Result.success(couponService.getCouponStats(id));
	}
}
