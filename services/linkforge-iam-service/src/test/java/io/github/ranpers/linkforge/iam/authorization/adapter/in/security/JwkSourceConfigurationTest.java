package io.github.ranpers.linkforge.iam.authorization.adapter.in.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JwkSourceConfigurationTest {

    @Test
    void shouldAdaptStoredSigningKeyToNimbusRsaKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        SigningKey signingKey = new SigningKey(
                "test-key",
                SigningAlgorithm.RS256,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
        );

        RSAKey rsaKey = JwkSourceConfiguration.toRsaKey(signingKey);

        assertEquals("test-key", rsaKey.getKeyID());
        assertEquals(JWSAlgorithm.RS256, rsaKey.getAlgorithm());
        assertEquals(KeyUse.SIGNATURE, rsaKey.getKeyUse());
        assertArrayEquals(keyPair.getPublic().getEncoded(), rsaKey.toRSAPublicKey().getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), rsaKey.toRSAPrivateKey().getEncoded());
    }
}
