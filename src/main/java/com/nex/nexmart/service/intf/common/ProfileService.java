package com.nex.nexmart.service.intf.common;

import com.nex.nexmart.model.dto.login.ChangePhoneNumberDTO;
import com.nex.nexmart.model.dto.login.PasswordUpdateDTO;
import com.nex.nexmart.model.dto.login.ProfileUpdateDTO;
import com.nex.nexmart.model.vo.LoginVO;
import jakarta.validation.Valid;

public interface ProfileService {
	/**
	 * 更新当前登录用户资料（用户名 / 手机 / 邮箱），校验格式与唯一性，并返回新 Token（用户名会写入 JWT）
	 */
	LoginVO updateProfile(Long userId, Integer role, @Valid ProfileUpdateDTO dto);

	void updatePassword(Long userId, Integer role, PasswordUpdateDTO dto);

	/**
	 * 发送修改手机号验证码（到日志）
	 */
	void sendVerificationCode(String originalPhone,String newPhone,Long userId);

	/**
	 * 通过验证码修改手机号
	 */
	void changePhoneNumber(@Valid ChangePhoneNumberDTO dto, Long userId);
}
