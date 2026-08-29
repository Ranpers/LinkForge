package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyStore;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisSigningKeyStore implements SigningKeyStore {

    /** 固定事务级咨询锁，由应用服务持有事务。 */
    private static final long INITIALIZATION_LOCK_ID = 0x4C464A574BL;

    private final SigningKeyMapper signingKeyMapper;

    public MybatisSigningKeyStore(SigningKeyMapper signingKeyMapper) {
        this.signingKeyMapper = signingKeyMapper;
    }

    @Override
    public void lockForInitialization() {
        signingKeyMapper.acquireInitializationLock(INITIALIZATION_LOCK_ID);
    }

    @Override
    public Optional<SigningKey> findActive() {
        return Optional.ofNullable(signingKeyMapper.findActive()).map(MybatisSigningKeyStore::toDomain);
    }

    @Override
    public void save(SigningKey signingKey) {
        int affectedRows = signingKeyMapper.insert(toDataObject(signingKey));
        if (affectedRows != 1) {
            throw new IllegalStateException("授权服务器签名密钥保存失败");
        }
    }

    private static SigningKey toDomain(SigningKeyDO dataObject) {
        final SigningAlgorithm algorithm;
        try {
            algorithm = SigningAlgorithm.valueOf(dataObject.algorithm());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("不支持的签名算法: " + dataObject.algorithm(), exception);
        }
        return new SigningKey(
                dataObject.keyId(),
                algorithm,
                dataObject.publicKeyDer(),
                dataObject.privateKeyDer()
        );
    }

    private static SigningKeyDO toDataObject(SigningKey signingKey) {
        return new SigningKeyDO(
                signingKey.keyId(),
                signingKey.algorithm().name(),
                signingKey.publicKeyDer(),
                signingKey.privateKeyDer()
        );
    }
}
