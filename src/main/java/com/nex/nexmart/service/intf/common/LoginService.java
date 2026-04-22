package com.nex.nexmart.service.intf.common;

import com.nex.nexmart.model.dto.login.ChangePhoneNumberDTO;
import com.nex.nexmart.model.dto.login.LoginDTO;
import com.nex.nexmart.model.dto.login.ResetPasswordDTO;
import com.nex.nexmart.model.dto.login.UserRegisterDTO;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import jakarta.validation.Valid;

public interface LoginService {

	void register(@Valid UserRegisterDTO dto);

	LoginVO login(@Valid LoginDTO dto);

	UserVO getCurrentUserInfo(Long userId);

	/**
	 * 发送重置密码验证码（到日志）
	 */
	void sendVerificationCode(String phone);
	/**
	 * 通过验证码重置密码
	 */
	void resetPassword(@Valid ResetPasswordDTO dto);

}
