package io.github.ranpers.linkforge.iam.control.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControlEventTraceIdTest {

    @Test
    void preservesValidValueAndTreatsNullAsMissing() {
        String value = "a".repeat(ControlEventTraceId.MAX_LENGTH);

        assertEquals(value, new ControlEventTraceId(value).value());
        assertNull(ControlEventTraceId.fromNullable(null));
    }

    @Test
    void countsUnicodeCodePointsInsteadOfUtf16Units() {
        String value = "😀".repeat(ControlEventTraceId.MAX_LENGTH);

        assertEquals(value, new ControlEventTraceId(value).value());
    }

    @Test
    void rejectsBlankAndOversizedValues() {
        assertThrows(
                InvalidControlEventTraceIdException.class,
                () -> new ControlEventTraceId("   ")
        );
        assertThrows(
                InvalidControlEventTraceIdException.class,
                () -> new ControlEventTraceId("a".repeat(ControlEventTraceId.MAX_LENGTH + 1))
        );
    }
}
