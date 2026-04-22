package com.nex.nexmart.controller.user.points;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.ExchangeResultVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsLogVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.PointsMallItemService;
import com.nex.nexmart.service.intf.checkinPoint.UserPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-积分商城")
@RestController("UserPointsMallController")
@RequestMapping("/api/user/points")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('USER')")
public class PointsMallController {

	private final PointsMallItemService pointsMallItemService;
	private final UserPointsService userPointsService;

	@Operation(summary = "积分商城列表")
	@GetMapping("/mall")
	public Result<PointsMallVO> mall(
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("查询积分商城 userId={}", userId);
		return Result.success(pointsMallItemService.getUserMall(userId));
	}

	@Operation(summary = "兑换券")
	@PostMapping("/exchange/{itemId}")
	public Result<ExchangeResultVO> exchange(
			@PathVariable Long itemId,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("积分兑换 userId={} itemId={}", userId, itemId);
		return Result.success(pointsMallItemService.exchange(userId, itemId));
	}

	@Operation(summary = "积分流水")
	@GetMapping("/logs")
	public Result<PageResult<PointsLogVO>> logs(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("查询积分流水 userId={} current={} size={}", userId, current, size);
		return Result.success(userPointsService.getLogs(userId, current, size));
	}
}
