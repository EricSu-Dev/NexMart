package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.home.BannerDTO;
import com.nex.nexmart.model.vo.home.BannerVO;
import com.nex.nexmart.service.intf.home.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-轮播图接口")
@RestController("adminBannerController")
@RequestMapping("/api/admin/banner")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class BannerController {

	private final BannerService bannerService;

	/** 查询全部轮播图（含下架） */
	@GetMapping
	@Operation(summary = "查询全部轮播图（含下架）")
	public Result<List<BannerVO>> list() {
		log.info("查询全部轮播图（含下架）");
		return Result.success(bannerService.getAllBanners());
	}

	/** 新增轮播图 */
	@PostMapping
	@Operation(summary = "新增轮播图")
	public Result<Void> add(@RequestBody BannerDTO dto) {
		log.info("新增轮播图：{}", dto);
		bannerService.addBanner(dto);
		return Result.success();
	}

	/** 编辑轮播图 */
	@PutMapping("/{id}")
	@Operation(summary = "编辑轮播图")
	public Result<Void> update(@PathVariable Long id, @RequestBody BannerDTO dto) {
		log.info("编辑轮播图：{}", id);
		bannerService.updateBanner(id, dto);
		return Result.success();
	}

	/** 删除轮播图 */
	@DeleteMapping("/{id}")
	@Operation(summary = "删除轮播图")
	public Result<Void> delete(@PathVariable Long id) {
		log.info("删除轮播图：{}", id);
		bannerService.deleteBanner(id);
		return Result.success();
	}

	/** 上下架 */
	@PutMapping("/{id}/status")
	@Operation(summary = "轮播图上下架")
	public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
		log.info("上下架轮播图：{}", id);
		bannerService.updateStatus(id, status);
		return Result.success();
	}
	@GetMapping("/{id}")
	@Operation(summary = "轮播图详情")
	public Result<BannerVO> details(@PathVariable Long id) {
		log.info("查询轮播图详情：{}", id);
		return Result.success(bannerService.getDetails(id));
	}
}
