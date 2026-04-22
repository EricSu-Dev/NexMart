package com.nex.nexmart.model.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改手机号请求参数")
public class ChangePhoneNumberDTO {
	@Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String originPhoneNumber;

	@Schema(description = "新手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "新手机号不能为空")
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String newPhoneNumber;

	@Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "验证码不能为空")
	private String code;
}
