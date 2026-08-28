package io.github.ranpers.linkforge.iam.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValueObjectTest {

    @Test
    void shouldNormalizeUsernameAndOptionalEmail() {
        assertEquals("alice_01", Username.of("  alice_01  ").value());
        assertEquals("alice@example.com", EmailAddress.optional(" Alice@Example.COM ").value());
        assertNull(EmailAddress.optional("  ").value());
    }

    @Test
    void shouldRejectInvalidUsernameAndEmail() {
        assertThrows(InvalidUserDataException.class, () -> Username.of("a b"));
        assertThrows(InvalidUserDataException.class, () -> EmailAddress.optional("not-an-email"));
    }

    @Test
    void shouldRejectPasswordBeyondBcryptUtf8Boundary() {
        String seventyFiveUtf8Bytes = "界".repeat(25);

        assertThrows(
                InvalidUserDataException.class,
                () -> RawPassword.of(seventyFiveUtf8Bytes)
        );
    }
}
