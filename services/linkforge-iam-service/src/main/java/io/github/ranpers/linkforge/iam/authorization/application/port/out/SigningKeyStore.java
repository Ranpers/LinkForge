package io.github.ranpers.linkforge.iam.authorization.application.port.out;

import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;

import java.util.Optional;

public interface SigningKeyStore {

    /** 串行化多实例首次初始化；锁的具体机制由输出适配器决定。 */
    void lockForInitialization();

    Optional<SigningKey> findActive();

    void save(SigningKey signingKey);
}
