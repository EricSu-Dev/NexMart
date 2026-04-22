package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.review.ReviewCommentCreateDTO;
import com.nex.nexmart.model.dto.review.ReviewCreateDTO;
import com.nex.nexmart.model.vo.review.ReviewCommentVO;
import com.nex.nexmart.model.vo.review.ReviewLikeVO;
import com.nex.nexmart.model.vo.review.ReviewVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-评价接口")
@RestController("userReviewController")
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(summary = "提交评价")
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
	public Result<Void> create(@Valid @RequestBody ReviewCreateDTO dto,
	                           @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Review create userId={} orderItemId={} productId={}", userId, dto.getOrderItemId(), dto.getProductId());
		reviewService.createReview(dto, userId);
		return Result.success();
	}

	@Operation(summary = "商品评价列表")
	@GetMapping
	public Result<PageResult<ReviewVO>> list(
			@RequestParam(value = "product_id") Long productId,
			@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails == null ? null : userDetails.getUser().getId();
		return Result.success(reviewService.pageByProduct(productId, page, size, userId));
	}

	@Operation(summary = "评价详情")
	@GetMapping("/{reviewId}")
	public Result<ReviewVO> detail(@PathVariable Long reviewId,
	                               @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails == null ? null : userDetails.getUser().getId();
		return Result.success(reviewService.getReviewDetail(reviewId, userId));
	}

	@Operation(summary = "点赞/取消点赞")
	@PostMapping("/{reviewId}/like")
	@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
	public Result<ReviewLikeVO> like(@PathVariable Long reviewId,
	                                 @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		return Result.success(reviewService.toggleLike(reviewId, userId));
	}

	@Operation(summary = "评价评论列表")
	@GetMapping("/{reviewId}/comments")
	public Result<List<ReviewCommentVO>> comments(@PathVariable Long reviewId) {
		return Result.success(reviewService.listComments(reviewId));
	}

	@Operation(summary = "发表评论或回复")
	@PostMapping("/{reviewId}/comments")
	@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
	public Result<Void> addComment(@PathVariable Long reviewId,
	                               @Valid @RequestBody ReviewCommentCreateDTO dto,
	                               @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		reviewService.addComment(reviewId, dto, userId);
		return Result.success();
	}

	@Operation(summary = "删除评价")
	@DeleteMapping("/{reviewId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
	public Result<Void> deleteReview(@PathVariable Long reviewId,
	                                 @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		reviewService.deleteReview(reviewId, userId);
		return Result.success();
	}

	@Operation(summary = "删除评价评论")
	@DeleteMapping("/{reviewId}/comments/{commentId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
	public Result<Void> deleteComment(@PathVariable Long reviewId,
	                                  @PathVariable Long commentId,
	                                  @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		reviewService.deleteComment(reviewId, commentId, userId);
		return Result.success();
	}
}
