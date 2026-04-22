package com.nex.nexmart.model.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {

	@NotBlank(message = "用户名不能为空")
	@Size(min = 3, max = 20, message = "用户名长度 3-20 位")
	private String username;

	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;

	/** 选填；有值时由服务层校验格式与唯一性 */
	@Size(max = 64, message = "邮箱过长")
	private String email;

	/** 头像地址（可选） */
	@Size(max = 255, message = "头像地址过长")
	private String avatarUrl;

	@Size(max = 33, message = "个性签名过长")
	private String profileSignature;
}
