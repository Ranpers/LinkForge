package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.UUID;

/**
 * 入港用例的出参模型:不外泄领域对象与密码散列
 */
public record RegisteredUser(UUID id, String username, String email) {
}
