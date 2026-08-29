package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import java.util.Objects;

/** MyBatis 查询结果，只在持久化适配器内使用。 */
public record SigningKeyDO(
        String keyId,
        String algorithm,
        String publicKeyDer,
        String privateKeyDer
) {

    public SigningKeyDO {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(publicKeyDer, "publicKeyDer");
        Objects.requireNonNull(privateKeyDer, "privateKeyDer");
    }
}
