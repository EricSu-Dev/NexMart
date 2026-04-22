package com.nex.nexmart.controller.user.points;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.checkinPoint.CheckinResultVO;
import com.nex.nexmart.model.vo.checkinPoint.CheckinStatusVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.checkinPoint.UserCheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "用户端-签到接口")
@RestController
@RequestMapping("/api/user/checkin")
@RequiredArgsConstructor
@Slf4j
public class CheckinController {

	private final UserCheckinService userCheckinService;

	@Operation(summary = "执行签到")
	@PostMapping
	public Result<CheckinResultVO> checkin(
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("用户签到 userId={}", userId);
		return Result.success(userCheckinService.checkin(userId));
	}

	@Operation(summary = "查询签到状态")
	@GetMapping("/status")
	public Result<CheckinStatusVO> status(
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		LocalDate target = (year != null && month != null)
				? LocalDate.of(year, month, 1)
				: LocalDate.now();
		log.info("查询签到状态 userId={} year={} month={}", userId, year, month);
		return Result.success(userCheckinService.getStatus(userId, target));
	}
}

