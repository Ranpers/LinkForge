package io.github.ranpers.linkforge.link.creation.adapter.out.shortcode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureRandomShortCodeGeneratorTest {

    @Test
    void generatesTenBase62Characters() {
        String code = new SecureRandomShortCodeGenerator().nextCode();

        assertTrue(code.matches("[0-9A-Za-z]{10}"));
    }
}
