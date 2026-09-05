package io.github.ranpers.linkforge.iam.control.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControlEventTraceIdTest {

    private static final int[] WIRE_WHITESPACE_CODE_POINTS = {
            0x0009, 0x000A, 0x000B, 0x000C, 0x000D,
            0x0020, 0x0085, 0x00A0, 0x1680,
            0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005,
            0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
            0x2028, 0x2029, 0x202F, 0x205F, 0x3000
    };

    @Test
    void preservesValidValueAndTreatsNullAsMissing() {
        String value = "trace-123";

        assertEquals(value, new ControlEventTraceId(value).value());
        assertNull(ControlEventTraceId.fromNullable(null));
    }

    @Test
    void countsUnicodeCodePointsInsteadOfUtf16Units() {
        String value = "😀".repeat(ControlEventTraceId.MAX_LENGTH);

        assertEquals(value, new ControlEventTraceId(value).value());
        assertThrows(
                InvalidControlEventTraceIdException.class,
                () -> new ControlEventTraceId("😀".repeat(ControlEventTraceId.MAX_LENGTH + 1))
        );
    }

    @Test
    void rejectsEmptyAndEveryWireWhitespaceCodePoint() {
        assertThrows(
                InvalidControlEventTraceIdException.class,
                () -> new ControlEventTraceId("")
        );
        for (int codePoint : WIRE_WHITESPACE_CODE_POINTS) {
            String whitespace = new String(Character.toChars(codePoint));
            assertThrows(
                    InvalidControlEventTraceIdException.class,
                    () -> new ControlEventTraceId(whitespace),
                    () -> "应拒绝 wire 空白码点 U+%04X".formatted(codePoint)
            );
        }
    }
}
