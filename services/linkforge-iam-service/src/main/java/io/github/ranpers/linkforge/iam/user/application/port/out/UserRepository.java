package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.user.domain.User;
import io.github.ranpers.linkforge.iam.user.domain.Username;

/**
 * 出港(driven port):用户持久化。由 out/persistence 适配器以 MyBatis-Plus 实现
 */
public interface UserRepository {

    boolean existsByUsername(Username username);

    /** 只持久化用户聚合，不执行角色绑定等隐藏副作用。 */
    void save(User user);
}
