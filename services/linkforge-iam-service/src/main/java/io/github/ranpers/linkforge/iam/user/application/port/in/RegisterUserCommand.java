package io.github.ranpers.linkforge.iam.user.application.port.in;

/**
 * 入港用例的入参模型:由适配器校验后转换而来,核心层不携带 Bean Validation 注解
 */
public record RegisterUserCommand(String username, String password, String email) {
}
