package com.nex.nexmart.model.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端添加员工：手机必填，邮箱选填
 */
@Data
public class EmployeeRegisterDTO {

    @NotBlank(message = "员工名不能为空")
    @Size(min = 3, max = 20, message = "员工名长度 3-20 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 选填；有值时由服务层校验格式与唯一性 */
    @Size(max = 64, message = "邮箱过长")
    private String email;
}
