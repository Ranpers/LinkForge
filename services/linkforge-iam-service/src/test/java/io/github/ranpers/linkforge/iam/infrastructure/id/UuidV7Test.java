package io.github.ranpers.linkforge.iam.infrastructure.id;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UuidV7Test {

    @Test
    void shouldGenerateVersion7Uuid() {
        UUID uuid = UuidV7.generate();

        assertEquals(7, (uuid.toString().charAt(14)) - '0');
    }

    @Test
    void shouldGenerateStrictlyMonotonicUuidsInSameProcess() {
        UUID previous = UuidV7.generate();
        for (int i = 0; i < 100; i++) {
            UUID current = UuidV7.generate();
            assertNotEquals(previous, current);
            previous = current;
        }
    }
}
