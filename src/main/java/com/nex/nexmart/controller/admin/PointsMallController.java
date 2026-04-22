package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.PointsMallItemDTO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallItemVO;
import com.nex.nexmart.service.intf.PointsMallItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-积分商城")
@RestController("AdminPointsMallController")
@RequestMapping("/api/admin/points/mall")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
public class PointsMallController {

	private final PointsMallItemService pointsMallItemService;

	@Operation(summary = "兑换项列表")
	@GetMapping
	public Result<List<PointsMallItemVO>> list(
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer discountType) {
		log.info("查询积分商城列表 status={}", status);
		return Result.success(pointsMallItemService.listItems(keyword, discountType, status));
	}

	@Operation(summary = "创建兑换项")
	@PostMapping
	public Result<Void> create(@Valid @RequestBody PointsMallItemDTO dto) {
		log.info("创建积分商城兑换项 couponId={} pointsCost={}", dto.getCouponId(), dto.getPointsCost());
		pointsMallItemService.createItem(dto);
		return Result.success();
	}

	@Operation(summary = "上下架兑换项")
	@PatchMapping("/{id}/status")
	public Result<Void> updateStatus(
			@PathVariable Long id,
			@RequestParam Integer status) {
		log.info("更新兑换项状态 id={} status={}", id, status);
		pointsMallItemService.updateStatus(id, status);
		return Result.success();
	}

	@Operation(summary = "修改所需积分")
	@PatchMapping("/{id}/points-cost")
	public Result<Void> updatePointsCost(
			@PathVariable Long id,
			@RequestParam @Min(1) Integer pointsCost) {
		log.info("修改兑换项积分 id={} pointsCost={}", id, pointsCost);
		pointsMallItemService.updatePointsCost(id, pointsCost);
		return Result.success();
	}

	@Operation(summary = "删除兑换项")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		log.info("删除兑换项 id={}", id);
		pointsMallItemService.deleteItem(id);
		return Result.success();
	}
}
