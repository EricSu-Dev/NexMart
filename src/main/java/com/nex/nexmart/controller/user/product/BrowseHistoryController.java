package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.product.ProductBrowseHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-浏览记录")
@RestController
@RequestMapping("/api/user/browse-history")
@RequiredArgsConstructor
@Slf4j
public class BrowseHistoryController {

	private final ProductBrowseHistoryService productBrowseHistoryService;

	/** 记录浏览（用户进入商品详情页时调用） */
	@PostMapping
	@Operation(summary = "记录浏览")
	public Result<Void> record(Long productId,
	                           @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("record: userId={}, productId={}", userId, productId);
		productBrowseHistoryService.record(userId, productId);
		return Result.success();
	}

	/** 浏览记录列表（分页） */
	@GetMapping
	@Operation(summary = "浏览记录列表")
	public Result<PageResult<BrowseHistoryVO>> list(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("list: userId={}, page={}, size={}", userId, page, size);
		return Result.success(productBrowseHistoryService.getHistoryPage(userId, page, size, keyword, categoryId));
	}

	/** 删除单条记录 */
	@DeleteMapping("/{id}")
	@Operation(summary = "删除单条记录")
	public Result<Void> removeOne(@PathVariable Long id,
	                              @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("removeOne: userId={}, id={}", userId);
		productBrowseHistoryService.removeOne(userId, id);
		return Result.success();
	}

	/** 清空全部记录 */
	@DeleteMapping
	@Operation(summary = "清空全部记录")
	public Result<Void> removeAll(@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("removeAll: userId={}", userId);
		productBrowseHistoryService.removeAll(userId);
		return Result.success();
	}
}
