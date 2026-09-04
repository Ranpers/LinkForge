package io.github.ranpers.linkforge.link.management.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedTargetUrlTest {

    @Test
    void acceptsAndNormalizesHttpUrl() {
        assertEquals(
                "https://example.com/path",
                new ManagedTargetUrl(" https://example.com/path ").value()
        );
    }

    @Test
    void rejectsNonHttpAndHostlessUrls() {
        assertThrows(
                InvalidManagedTargetUrlException.class,
                () -> new ManagedTargetUrl("javascript:alert(1)")
        );
        assertThrows(
                InvalidManagedTargetUrlException.class,
                () -> new ManagedTargetUrl("https:///missing-host")
        );
    }
}
