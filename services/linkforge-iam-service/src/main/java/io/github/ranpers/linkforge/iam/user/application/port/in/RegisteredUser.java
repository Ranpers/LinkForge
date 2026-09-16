package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.UUID;

/**
 * 注册成功后的公开结果，不外泄领域对象与密码散列。
 *
 * @param id 新用户的稳定标识
 * @param username 已规范化的用户名
 * @param email 已规范化的邮箱；注册时未提供则为 {@code null}
 */
public record RegisteredUser(UUID id, String username, String email) {
}
