package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyStore;
import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyProtector;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisSigningKeyStore implements SigningKeyStore {

    private static final long INITIALIZATION_LOCK_ID = 0x4C464A574BL;

    private final SigningKeyMapper signingKeyMapper;
    private final SigningKeyProtector signingKeyProtector;

    public MybatisSigningKeyStore(
            SigningKeyMapper signingKeyMapper,
            SigningKeyProtector signingKeyProtector
    ) {
        this.signingKeyMapper = signingKeyMapper;
        this.signingKeyProtector = signingKeyProtector;
    }

    @Override
    public void lockForInitialization() {
        signingKeyMapper.acquireInitializationLock(INITIALIZATION_LOCK_ID);
    }

    @Override
    public Optional<SigningKey> findActive() {
        return Optional.ofNullable(signingKeyMapper.findActive()).map(this::toDomain);
    }

    @Override
    public void save(SigningKey signingKey) {
        int affectedRows = signingKeyMapper.insert(toDataObject(signingKey));
        if (affectedRows != 1) {
            throw new IllegalStateException("授权服务器签名密钥保存失败");
        }
    }

    private SigningKey toDomain(SigningKeyDO dataObject) {
        final SigningAlgorithm algorithm;
        try {
            algorithm = SigningAlgorithm.valueOf(dataObject.algorithm());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("不支持的签名算法: " + dataObject.algorithm(), exception);
        }
        String privateKeyDer = signingKeyProtector.unprotect(
                dataObject.keyId(), dataObject.protectedPrivateKey()
        );
        return new SigningKey(
                dataObject.keyId(),
                algorithm,
                dataObject.publicKeyDer(),
                privateKeyDer
        );
    }

    private SigningKeyDO toDataObject(SigningKey signingKey) {
        return new SigningKeyDO(
                signingKey.keyId(),
                signingKey.algorithm().name(),
                signingKey.publicKeyDer(),
                signingKeyProtector.protect(signingKey.keyId(), signingKey.privateKeyDer())
        );
    }
}
