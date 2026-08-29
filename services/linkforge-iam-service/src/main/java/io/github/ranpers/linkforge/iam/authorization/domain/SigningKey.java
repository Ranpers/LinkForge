package io.github.ranpers.linkforge.iam.authorization.domain;

import java.util.Objects;

/**
 * 授权服务器签名密钥。领域模型只表达密钥材料，不依赖 Nimbus、MyBatis 或数据库模型。
 */
public record SigningKey(
        String keyId,
        SigningAlgorithm algorithm,
        String publicKeyDer,
        String privateKeyDer
) {

    public SigningKey {
        keyId = requireText(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        publicKeyDer = requireText(publicKeyDer, "publicKeyDer");
        privateKeyDer = requireText(privateKeyDer, "privateKeyDer");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value;
    }
}
