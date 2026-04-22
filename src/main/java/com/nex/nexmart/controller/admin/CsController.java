package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.cs.CsMessageVO;
import com.nex.nexmart.model.vo.cs.CsSessionVO;
import com.nex.nexmart.service.intf.cs.CsMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-客服接口")
@RestController("AdminCsController")
@RequestMapping("/api/admin/cs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
public class CsController {
	private final CsMessageService csMessageService;

	// 会话列表
	@GetMapping("/session/list")
	@Operation(summary = "会话列表")
	public Result<PageResult<CsSessionVO>> getSessions(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) String keyword
	) {
		return Result.success(csMessageService.getSessions(current, size, keyword));
	}

	// 拉取历史消息（顺手标已读）
	@GetMapping("/message/history")
	@Operation(summary = "拉取历史消息")
	public Result<List<CsMessageVO>> getMessages(@RequestParam Long sessionId) {
		return Result.success(csMessageService.getMessages(sessionId, 2));
	}
}
