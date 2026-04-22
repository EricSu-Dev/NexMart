package com.nex.nexmart.controller.common;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.login.ChangePhoneNumberDTO;
import com.nex.nexmart.model.dto.login.PasswordUpdateDTO;
import com.nex.nexmart.model.dto.login.ProfileUpdateDTO;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.common.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通用-个人信息管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/profile")
@PreAuthorize("hasAnyRole('USER','ADMIN','BOSS')")
@Slf4j
public class ProfileController {

	private final ProfileService profileService;

	@Operation(summary = "修改个人资料（用户名、手机、邮箱）")
	@PutMapping
	public Result<LoginVO> updateProfile(
			@AuthenticationPrincipal SecurityUserDetails userDetails,
			@RequestBody @Valid ProfileUpdateDTO dto) {
		log.info("Admin update profile userId={}", userDetails.getUser().getId());
		return Result.success(profileService.updateProfile(userDetails.getUser().getId(),userDetails.getUser().getRole(), dto));
	}

	@Operation(summary = "修改个人密码")
	@PutMapping("/password")
	public Result<Void> updatePassword(
			@AuthenticationPrincipal SecurityUserDetails userDetails,
			@RequestBody @Valid PasswordUpdateDTO dto) {
		log.info("User update password: {}", userDetails.getUsername());
		profileService.updatePassword(userDetails.getUser().getId(),userDetails.getUser().getRole(), dto);
		return Result.success();
	}

	@Operation(summary = "获取修改手机号验证码")
	@PostMapping("/change-phone-code")
	public Result<Void> sendResetCode(@RequestParam String originPhone, @RequestParam String newPhone,
	                                  @AuthenticationPrincipal SecurityUserDetails securityUserDetails) {
		Long userId = securityUserDetails.getUser().getId();
		log.info("向{}发送申请修改手机号的验证码",originPhone);
		profileService.sendVerificationCode(originPhone, newPhone, userId);
		return Result.success("验证码已发送，请查看服务器日志", null);
	}

	@Operation(summary = "通过验证码修改手机号")
	@PostMapping("/change-phone")
	public Result<Void> changePhoneNumber(@Valid @RequestBody ChangePhoneNumberDTO dto,
	                                      @AuthenticationPrincipal SecurityUserDetails securityUserDetails) {
		Long userId = securityUserDetails.getUser().getId();
		log.info("更改手机号:{}",dto.getOriginPhoneNumber());
		profileService.changePhoneNumber(dto, userId);
		return Result.success("修改手机号成功", null);
	}
}
