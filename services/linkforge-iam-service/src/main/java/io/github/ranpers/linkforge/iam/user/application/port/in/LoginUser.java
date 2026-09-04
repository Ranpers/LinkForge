package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.Set;
import java.util.UUID;

/**
 * 登录用例输出模型，不暴露 Spring Security 类型。
 *
 * @param id 用户的稳定标识，同时作为签发 JWT 的 subject
 * @param username 规范化用户名
 * @param passwordHash 带算法前缀的密码散列，不得写入日志或 API 响应
 * @param enabled 是否允许通过账号状态校验
 * @param roles 不可变的角色码集合
 * @param permissions 不可变的权限码集合
 */
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
