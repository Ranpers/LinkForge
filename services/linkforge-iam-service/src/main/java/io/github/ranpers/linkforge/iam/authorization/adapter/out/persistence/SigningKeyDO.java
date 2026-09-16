package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import java.util.Objects;

public record SigningKeyDO(
        String keyId,
        String algorithm,
        String publicKeyDer,
        String protectedPrivateKey
) {

    public SigningKeyDO {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(publicKeyDer, "publicKeyDer");
        Objects.requireNonNull(protectedPrivateKey, "protectedPrivateKey");
    }
}
