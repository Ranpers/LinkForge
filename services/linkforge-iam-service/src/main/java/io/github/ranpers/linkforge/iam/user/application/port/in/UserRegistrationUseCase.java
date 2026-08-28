package io.github.ranpers.linkforge.iam.user.application.port.in;

/**
 * 入港(driving port):注册用例。驱动侧(web 适配器、未来的管理后台等)只依赖本接口
 */
public interface UserRegistrationUseCase {

    RegisteredUser register(RegisterUserCommand command);
}
