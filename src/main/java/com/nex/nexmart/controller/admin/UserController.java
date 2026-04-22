package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class UserController {

	private final UserService userService;

	@Operation(summary = "分页查询用户列表")
	@GetMapping("/page")
	public Result<PageResult<UserVO>> page(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) String keyword
			) {
		log.info("Admin user page current={} size={} keyword={}", current, size, keyword);
		return Result.success(userService.pageUsers(current, size, keyword));
	}

	@Operation(summary = "启用 / 禁用用户")
	@PutMapping("/{id}/status")
	public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
		log.info("Admin user update status id={} status={}", id, status);
		userService.updateUserStatus(id, status);
		return Result.success();
	}
}
