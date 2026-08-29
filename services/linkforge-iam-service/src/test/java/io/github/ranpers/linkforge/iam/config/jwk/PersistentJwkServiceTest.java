package io.github.ranpers.linkforge.iam.config.jwk;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistentJwkServiceTest {

    @Test
    void shouldRoundTripStoredRsaKeyMaterial() throws Exception {
        RSAKey original = rsaKey("test-key");

        RSAKey restored = PersistentJwkService.decode(PersistentJwkService.encode(original));

        assertEquals(original.getKeyID(), restored.getKeyID());
        assertEquals(JWSAlgorithm.RS256, restored.getAlgorithm());
        assertEquals(KeyUse.SIGNATURE, restored.getKeyUse());
        assertArrayEquals(original.toRSAPublicKey().getEncoded(), restored.toRSAPublicKey().getEncoded());
        assertArrayEquals(original.toRSAPrivateKey().getEncoded(), restored.toRSAPrivateKey().getEncoded());
    }

    @Test
    void shouldReuseActiveKeyWithoutGeneratingAnother() throws Exception {
        JwkKeyRepository repository = mock(JwkKeyRepository.class);
        StoredJwk storedJwk = PersistentJwkService.encode(rsaKey("existing-key"));
        when(repository.findActive()).thenReturn(Optional.of(storedJwk));

        RSAKey loaded = new PersistentJwkService(repository).loadOrCreate();

        assertEquals("existing-key", loaded.getKeyID());
        verify(repository).acquireInitializationLock();
        verify(repository, never()).save(any());
    }

    private static RSAKey rsaKey(String keyId) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }
}
