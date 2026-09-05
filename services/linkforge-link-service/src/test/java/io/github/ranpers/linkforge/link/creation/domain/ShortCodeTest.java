package io.github.ranpers.linkforge.link.creation.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShortCodeTest {

    @Test
    void acceptsGeneratedBase62Code() {
        ShortCode code = ShortCode.generated("0aZ19BcDeF");

        assertEquals("0aZ19BcDeF", code.value());
        assertEquals(ShortCodeType.GENERATED, code.type());
    }

    @Test
    void preservesCustomCodeCase() {
        assertEquals("My_Docs-1", ShortCode.custom("My_Docs-1").value());
    }

    @Test
    void rejectsInvalidGeneratedLength() {
        assertThrows(
                InvalidShortLinkException.class,
                () -> ShortCode.generated("TooShort")
        );
    }

    @Test
    void rejectsReservedCustomCodeIgnoringCase() {
        assertThrows(
                InvalidShortLinkException.class,
                () -> ShortCode.custom("HeAlTh")
        );
    }

    @Test
    void rejectsCustomWhitespaceInsteadOfNormalizingIt() {
        assertThrows(
                InvalidShortLinkException.class,
                () -> ShortCode.custom(" docs ")
        );
    }
}
