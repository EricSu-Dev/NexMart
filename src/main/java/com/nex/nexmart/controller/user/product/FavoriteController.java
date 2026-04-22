package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.product.ProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "用户端-我的收藏")
@RestController
@RequestMapping("/api/user/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

	private final ProductFavoriteService favoriteService;

	/** 收藏 / 取消收藏（toggle） */
	@PostMapping("/{productId}/toggle")
	@Operation(summary = "收藏 / 取消收藏")
	public Result<Boolean> toggle(@PathVariable Long productId,
	                              @AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("toggle: userId={}, productId={}", userDetails.getUser().getId(), productId);
		Long userId = userDetails.getUser().getId();
		return Result.success(favoriteService.toggle(userId, productId));  // 返回操作后的收藏状态
	}

	/** 查询某商品是否已收藏 */
	@GetMapping("/{productId}/status")
	@Operation(summary = "查询某商品是否已收藏")
	public Result<Boolean> status(@PathVariable Long productId,
	                              @AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("status: userId={}, productId={}", userDetails.getUser().getId(), productId);
		boolean fav = favoriteService.isFavorite(userDetails.getUser().getId(), productId);
		return Result.success(fav);
	}

	/** 我的收藏列表（分页） */
	@GetMapping
	@Operation(summary = "我的收藏列表")
	public Result<PageResult<FavoriteProductVO>> list(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("list: userId={}, page={}, size={}, keyword={}", userDetails.getUser().getId(), page, size, keyword);
		return Result.success(favoriteService.getFavoritePage(userDetails.getUser().getId(), page, size, keyword, categoryId));
	}

	/** 批量取消收藏 */
	@DeleteMapping("/batch")
	@Operation(summary = "批量取消收藏")
	public Result<Void> removeBatch(@RequestBody List<Long> productIds,
	                                @AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("removeBatch: userId={}, productIds={}", userDetails.getUser().getId(), productIds);
		Long userId = userDetails.getUser().getId();
		favoriteService.removeFavoriteBatch(userId, productIds);
		return Result.success();
	}
}
