package io.github.ranpers.linkforge.iam.user.adapter.out.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UuidV7Test {

    @Test
    void shouldGenerateValidVersionSevenUuid() {
        UUID uuid = UuidV7.generate();

        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void shouldRemainUniqueAndStrictlyOrderedAcrossSequenceBoundary() {
        Set<UUID> generated = new HashSet<>();
        UUID previous = UuidV7.generate();
        generated.add(previous);

        for (int index = 0; index < 10_000; index++) {
            UUID current = UuidV7.generate();
            assertTrue(previous.compareTo(current) < 0, "UUIDv7 必须保持进程内严格递增");
            assertTrue(generated.add(current), "UUIDv7 不得重复");
            previous = current;
        }
    }
}
