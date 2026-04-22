package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.PromotionDTO;
import com.nex.nexmart.model.vo.PromotionVO;
import com.nex.nexmart.service.intf.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-优惠活动")
@RestController("AdminPromotionController")
@RequestMapping("/api/admin/promotion")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class PromotionController {

	private final PromotionService promotionService;

	@Operation(summary = "分页查询优惠活动")
	@GetMapping("/page")
	public Result<PageResult<PromotionVO>> page(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) Integer scope,
			@RequestParam(required = false) Integer stage) {
		log.info("promotion page current={} size={} status={} scope={}", current, size, status, scope);
		return Result.success(promotionService.pagePromotions(current, size, status, scope, stage));
	}

	@Operation(summary = "创建优惠活动")
	@PostMapping
	public Result<Void> create(
			@RequestBody @Valid PromotionDTO dto) {
		log.info("promotion create dto={}", dto);
		promotionService.createPromotion(dto);
		return Result.success();
	}

	@Operation(summary = "修改优惠活动")
	@PutMapping("/{id}")
	public Result<Void> update(
			@PathVariable Long id,
			@RequestBody @Valid PromotionDTO dto) {
		log.info("promotion update id={} dto={}", id, dto);
		promotionService.updatePromotion(id, dto);
		return Result.success();
	}

	@Operation(summary = "删除优惠活动")
	@DeleteMapping("/{id}")
	public Result<Void> delete(
			@PathVariable Long id) {
		log.info("promotion delete id={}", id);
		promotionService.deletePromotion(id);
		return Result.success();
	}

	@Operation(summary = "上下架优惠活动")
	@PatchMapping("/{id}/status")
	public Result<Void> updateStatus(
			@PathVariable Long id,
			@RequestParam Integer status) {
		log.info("promotion updateStatus id={} status={}", id, status);
		promotionService.updateStatus(id, status);
		return Result.success();
	}
}
