package com.nex.nexmart.model.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    /** 用户名或 11 位手机号 */
    @NotBlank(message = "请输入用户名或手机号")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
