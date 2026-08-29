package io.github.ranpers.linkforge.iam.config.jwk;

import java.util.Objects;

public record StoredJwk(
        String keyId,
        String algorithm,
        String publicKeyDer,
        String privateKeyDer
) {

    public StoredJwk {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(publicKeyDer, "publicKeyDer");
        Objects.requireNonNull(privateKeyDer, "privateKeyDer");
    }
}
