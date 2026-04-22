package com.nex.nexmart.controller.user;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.entity.cs.CsSession;
import com.nex.nexmart.model.vo.cs.CsMessageVO;
import com.nex.nexmart.model.vo.cs.CsOrderCardVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.cs.CsMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-客服接口")
@Slf4j
@RestController("UserCsController")
@RequestMapping("/api/user/cs")
@RequiredArgsConstructor
public class CsController {
	private final CsMessageService csMessageService;

	// 发起会话
	@PostMapping("/session/create")
	@Operation(summary = "发起会话")
	public Result<CsSession> createSession(
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		return Result.success(csMessageService.createSession(userId));
	}

	// 拉取历史消息
	@GetMapping("/session/{sessionId}/history")
	@Operation(summary = "拉取历史消息")
	public Result<List<CsMessageVO>> getMessages(@PathVariable Long sessionId) {
		//1代表用户，2代表客服
		return Result.success(csMessageService.getMessages(sessionId,1));
	}

	@GetMapping("/session/orderCards")
	@Operation(summary = "拉取订单列表")
	public Result<PageResult<CsOrderCardVO>> getOrderCards
			(@RequestParam(defaultValue = "1") long current,
			 @RequestParam(defaultValue = "10") long size,
			 @RequestParam(required = false) Integer status,
			 @RequestParam(required = false) String keyword,
			 @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("获取订单卡片分页列表 userId={} current={} size={} status={}", userId, current, size, status);
		return Result.success(csMessageService.getOrderCards(current, size, userId, status,keyword));
	}

	@GetMapping("/unreadCount")
	@Operation(summary = "获取未读消息数")
	public Result<Integer> getUnreadCount(@RequestParam Long sessionId) {
		log.info("获取未读消息数 sessionId={}", sessionId);
		return Result.success(csMessageService.getUnreadCount(sessionId));
	}
}
