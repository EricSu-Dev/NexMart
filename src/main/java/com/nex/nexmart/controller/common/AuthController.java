package com.nex.nexmart.controller.common;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.login.LoginDTO;
import com.nex.nexmart.model.dto.login.UserRegisterDTO;
import com.nex.nexmart.model.dto.login.ResetPasswordDTO;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.service.intf.common.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通用-登录认证模块")
@RestController
@RequestMapping("/api/common/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final LoginService loginService;

	@Operation(summary = "用户注册")
	@PostMapping("/register")
	public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
		log.info("Auth register username={}", dto.getUsername());
		loginService.register(dto);
		return Result.success("注册成功", null);
	}

	@Operation(summary = "用户登录，返回JWT Token")
	@PostMapping("/login")
	public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
		log.info("Auth login username={}", dto.getUsername());
		return Result.success(loginService.login(dto));
	}

	@Operation(summary = "获取当前登录用户信息")
	@GetMapping("/info")
	public Result<UserVO> info(Authentication authentication) {
		Long userId = (Long) authentication.getPrincipal();
		log.info("Auth info userId={}", userId);
		return Result.success(loginService.getCurrentUserInfo(userId));
	}

	@Operation(summary = "获取重置密码验证码")
	@PostMapping("/reset-code")
	public Result<Void> sendResetCode(@RequestParam String phone) {
		log.info("Auth reset-code phone={}", phone);
		loginService.sendVerificationCode(phone);
		return Result.success("验证码已发送，请查看服务器日志", null);
	}

	@Operation(summary = "通过验证码重置密码")
	@PostMapping("/reset-password")
	public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
		log.info("Auth reset-password phone={}", dto.getPhone());
		loginService.resetPassword(dto);
		return Result.success("密码重置成功", null);
	}
}
