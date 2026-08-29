package io.github.ranpers.linkforge.iam.config.jwk;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JwkKeyRepository {

    /** 固定事务级咨询锁，串行化多实例首次启动时的密钥初始化。 */
    private static final long INITIALIZATION_LOCK_ID = 0x4C464A574BL;

    private final JwkKeyMapper jwkKeyMapper;

    JwkKeyRepository(JwkKeyMapper jwkKeyMapper) {
        this.jwkKeyMapper = jwkKeyMapper;
    }

    void acquireInitializationLock() {
        jwkKeyMapper.acquireInitializationLock(INITIALIZATION_LOCK_ID);
    }

    Optional<StoredJwk> findActive() {
        return Optional.ofNullable(jwkKeyMapper.findActive());
    }

    void save(StoredJwk jwk) {
        int affectedRows = jwkKeyMapper.insert(jwk);
        if (affectedRows != 1) {
            throw new IllegalStateException("授权服务器签名密钥保存失败");
        }
    }
}
