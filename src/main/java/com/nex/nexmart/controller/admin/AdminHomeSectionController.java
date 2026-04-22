package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.home.HomeSectionConfigDTO;
import com.nex.nexmart.model.entity.home.HomeSectionItem;
import com.nex.nexmart.model.vo.home.HomeSectionVO;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.service.intf.home.HomeSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="管理端-首页模块管理")
@RestController
@RequestMapping("/api/admin/home/section")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
public class AdminHomeSectionController {

	private final HomeSectionService homeSectionService;

	/** 查询所有模块配置 */
	@GetMapping
	@Operation(summary = "查询所有模块配置")
	public Result<List<HomeSectionVO>> list() {
		return Result.success(homeSectionService.getAllSections());
	}

	/** 更新模块配置（切换自动/手动、启用/禁用） */
	@PutMapping("/{type}")
	@Operation(summary = "更新模块配置（切换自动/手动、启用/禁用）")
	public Result<Void> updateConfig(@PathVariable Integer type,
	                                 @RequestBody HomeSectionConfigDTO dto) {
		homeSectionService.updateConfig(type, dto);
		return Result.success();
	}

	/** 查询手动模式下已配置的商品 */
	@GetMapping("/{type}/items")
	@Operation(summary = "查询手动模式下已配置的商品")
	public Result<List<ProductVO>> getItems(@PathVariable Integer type) {
		return Result.success(homeSectionService.getItems(type));
	}

	/** 手动模式添加商品 */
	@PostMapping("/{type}/items")
	@Operation(summary = "手动模式添加商品")
	public Result<Void> addItem(@PathVariable Integer type,
	                            @RequestBody Long productId) {
		homeSectionService.addItem(type, productId);
		return Result.success();
	}

	/** 移除商品 */
	@DeleteMapping("/{type}/items/{itemId}")
	@Operation(summary = "移除商品")
	public Result<Void> removeItem(@PathVariable Integer type,
	                               @PathVariable Long itemId) {
		homeSectionService.removeItem(type, itemId);
		return Result.success();
	}

	/** 调整商品排序 */
	@PutMapping("/{type}/items/sort")
	@Operation(summary = "调整商品排序")
	public Result<Void> updateSort(@PathVariable Integer type,
	                               @RequestBody List<HomeSectionItem> items) {
		homeSectionService.updateSort(type, items);
		return Result.success();
	}
}
