package io.github.ranpers.linkforge.iam.authorization.application;

import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyStore;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SigningKeyServiceTest {

    @Test
    void shouldReuseActiveSigningKey() {
        SigningKeyStore signingKeyStore = mock(SigningKeyStore.class);
        SigningKey existing = new SigningKey("existing-key", SigningAlgorithm.RS256, "public", "private");
        when(signingKeyStore.findActive()).thenReturn(Optional.of(existing));

        SigningKey loaded = new SigningKeyService(signingKeyStore).loadOrCreate();

        assertEquals(existing, loaded);
        verify(signingKeyStore).lockForInitialization();
        verify(signingKeyStore, never()).save(any());
    }

    @Test
    void shouldGenerateAndPersistSigningKeyWhenNoneExists() {
        SigningKeyStore signingKeyStore = mock(SigningKeyStore.class);
        when(signingKeyStore.findActive()).thenReturn(Optional.empty());

        SigningKey generated = new SigningKeyService(signingKeyStore).loadOrCreate();

        ArgumentCaptor<SigningKey> captor = ArgumentCaptor.forClass(SigningKey.class);
        verify(signingKeyStore).lockForInitialization();
        verify(signingKeyStore).save(captor.capture());
        assertEquals(generated, captor.getValue());
        assertEquals(SigningAlgorithm.RS256, generated.algorithm());
        assertTrue(Base64.getDecoder().decode(generated.publicKeyDer()).length > 0);
        assertTrue(Base64.getDecoder().decode(generated.privateKeyDer()).length > 0);
    }
}
