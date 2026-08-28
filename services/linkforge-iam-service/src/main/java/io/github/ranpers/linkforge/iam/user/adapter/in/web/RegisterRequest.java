package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.application.port.in.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。username 上限 64 对齐列宽；BCrypt 的 72 字节边界由核心 RawPassword 再次校验。
 */
public record RegisterRequest(

        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,64}$", message = "用户名需为 4-64 位字母、数字或下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在 6-64 位之间")
        String password,

        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱最长 128 位")
        String email
) {

    RegisterUserCommand toCommand() {
        return new RegisterUserCommand(username, password, email);
    }
}
