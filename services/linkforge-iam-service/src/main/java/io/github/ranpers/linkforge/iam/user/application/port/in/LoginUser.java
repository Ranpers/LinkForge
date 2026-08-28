package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.Set;
import java.util.UUID;

/** 登录用例输出模型，不暴露 Spring Security 类型。 */
public record LoginUser(
        UUID id,
        String username,
        String passwordHash,
        boolean enabled,
        Set<String> roles,
        Set<String> permissions
) {

    public LoginUser {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }
}
