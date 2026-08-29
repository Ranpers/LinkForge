package io.github.ranpers.linkforge.iam.authorization.application;

import io.github.ranpers.linkforge.iam.authorization.application.port.in.LoadSigningKeyUseCase;
import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyStore;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;

@Service
public class SigningKeyService implements LoadSigningKeyUseCase {

    private static final int RSA_KEY_SIZE = 2048;

    private final SigningKeyStore signingKeyStore;

    public SigningKeyService(SigningKeyStore signingKeyStore) {
        this.signingKeyStore = signingKeyStore;
    }

    /**
     * 同一事务内完成加锁、查询与创建，避免多实例首次启动时生成多把活动密钥。
     */
    @Transactional
    @Override
    public SigningKey loadOrCreate() {
        signingKeyStore.lockForInitialization();
        return signingKeyStore.findActive().orElseGet(this::generateAndSave);
    }

    private SigningKey generateAndSave() {
        SigningKey signingKey = generate();
        signingKeyStore.save(signingKey);
        return signingKey;
    }

    private static SigningKey generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = generator.generateKeyPair();
            return new SigningKey(
                    UUID.randomUUID().toString(),
                    SigningAlgorithm.RS256,
                    Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                    Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("RSA 签名密钥生成失败", exception);
        }
    }
}
