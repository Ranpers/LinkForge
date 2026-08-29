package io.github.ranpers.linkforge.iam.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @Test
    void shouldEncodeNewPasswordsWithBcryptAndMatchByPrefix() {
        String encoded = passwordEncoder.encode("TestPass_123");

        if (encoded != null) {
            assertTrue(encoded.startsWith("{bcrypt}"));
        }
        assertTrue(passwordEncoder.matches("TestPass_123", encoded));
    }

    @Test
    void shouldMatchPrefixedClientSecrets() {
        assertTrue(passwordEncoder.matches("dev-secret", "{noop}dev-secret"));
    }
}
