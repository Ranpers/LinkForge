package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.Optional;

/**
 * 为认证适配器提供不依赖 Spring Security 的登录快照。
 */
public interface LoadLoginUserUseCase {

    /**
     * 按规范化用户名读取用户、角色和权限的完整登录快照。
     *
     * @param username 外部输入的用户名；格式无效时按未找到处理
     * @return 找到时为登录快照，否则为空；冻结或删除用户仍会返回但标记为不可用
     */
    Optional<LoginUser> loadByUsername(String username);
}
