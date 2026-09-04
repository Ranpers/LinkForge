package io.github.ranpers.linkforge.iam.user.application.port.in;

import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;

/**
 * 原子地创建用户并分配默认 {@code USER} 角色。
 */
public interface UserRegistrationUseCase {

    /**
     * 注册新用户。保存用户与分配默认角色必须处于同一事务。
     *
     * @param command 未散列的注册输入
     * @return 不包含密码信息的注册结果
     * @throws InvalidUserDataException 用户名、密码或邮箱不满足领域约束时
     * @throws UsernameAlreadyExistsException 规范化用户名已存在时
     */
    RegisteredUser register(RegisterUserCommand command);
}
