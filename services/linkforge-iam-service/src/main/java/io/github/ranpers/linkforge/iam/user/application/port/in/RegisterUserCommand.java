package io.github.ranpers.linkforge.iam.user.application.port.in;

/**
 * 注册用例的框架无关输入。
 *
 * <p>应用层仍会建立值对象并验证业务不变量，不能假定调用方一定来自已执行
 * Bean Validation 的 Web 适配器。</p>
 *
 * @param username 待注册的用户名
 * @param password 未散列的原始密码，仅允许在注册调用链内短暂存在
 * @param email 可为空的邮箱地址
 */
public record RegisterUserCommand(String username, String password, String email) {
}
