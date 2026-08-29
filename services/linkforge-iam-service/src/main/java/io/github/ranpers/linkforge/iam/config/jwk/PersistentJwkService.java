package io.github.ranpers.linkforge.iam.config.jwk;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
class PersistentJwkService {

    private static final int RSA_KEY_SIZE = 2048;

    private final JwkKeyRepository repository;

    PersistentJwkService(JwkKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * 同一事务内持有 PostgreSQL 咨询锁，保证多实例并发首次启动时只生成一把活动密钥。
     */
    @Transactional
    public RSAKey loadOrCreate() {
        repository.acquireInitializationLock();
        return repository.findActive()
                .map(PersistentJwkService::decode)
                .orElseGet(this::generateAndSave);
    }

    private RSAKey generateAndSave() {
        RSAKey rsaKey = generate();
        repository.save(encode(rsaKey));
        return rsaKey;
    }

    static StoredJwk encode(RSAKey rsaKey) {
        try {
            return new StoredJwk(
                    rsaKey.getKeyID(),
                    JWSAlgorithm.RS256.getName(),
                    Base64.getEncoder().encodeToString(rsaKey.toRSAPublicKey().getEncoded()),
                    Base64.getEncoder().encodeToString(rsaKey.toRSAPrivateKey().getEncoded())
            );
        } catch (JOSEException exception) {
            throw new IllegalStateException("RSA JWK 编码失败", exception);
        }
    }

    static RSAKey decode(StoredJwk storedJwk) {
        if (!JWSAlgorithm.RS256.getName().equals(storedJwk.algorithm())) {
            throw new IllegalStateException("不支持的 JWK 算法: " + storedJwk.algorithm());
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(storedJwk.publicKeyDer()))
            );
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(storedJwk.privateKeyDer()))
            );
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(storedJwk.keyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("数据库中的 RSA JWK 无法解析", exception);
        }
    }

    private static RSAKey generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("RSA JWK 生成失败", exception);
        }
    }
}
