package io.github.ranpers.linkforge.link.control.contract;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkControlEventSchemaTest {

    private static final String SCHEMA_PATH =
            "contracts/link-control/v1/link-control-event.schema.json";
    private static final int[] WIRE_WHITESPACE_CODE_POINTS = {
            0x0009, 0x000A, 0x000B, 0x000C, 0x000D,
            0x0020, 0x0085, 0x00A0, 0x1680,
            0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005,
            0x2006, 0x2007, 0x2008, 0x2009, 0x200A,
            0x2028, 0x2029, 0x202F, 0x205F, 0x3000
    };

    @Test
    void acceptsNullNormalAndAtMost64UnicodeCodePoints() throws IOException {
        JsonNode traceIdSchema = loadTraceIdSchema();

        assertTrue(isValid(null, traceIdSchema));
        assertTrue(isValid("trace-123", traceIdSchema));
        assertTrue(isValid("😀".repeat(64), traceIdSchema));
        assertTrue(isValid(" \u00A0a\u3000 ", traceIdSchema));
        assertTrue(isValid("\u001C", traceIdSchema));
    }

    @Test
    void rejectsOversizedEmptyAndWireWhitespaceOnlyStrings() throws IOException {
        JsonNode traceIdSchema = loadTraceIdSchema();

        assertFalse(isValid("😀".repeat(65), traceIdSchema));
        assertFalse(isValid("", traceIdSchema));
        assertFalse(isValid("   ", traceIdSchema));
        for (int codePoint : WIRE_WHITESPACE_CODE_POINTS) {
            String whitespace = new String(Character.toChars(codePoint));
            assertFalse(
                    isValid(whitespace, traceIdSchema),
                    () -> "Schema 应拒绝 wire 空白码点 U+%04X".formatted(codePoint)
            );
        }
    }

    private static boolean isValid(String value, JsonNode schema) {
        if (value == null) {
            return supportsType(schema, "null");
        }
        int length = value.codePointCount(0, value.length());
        return supportsType(schema, "string")
                && length >= schema.get("minLength").intValue()
                && length <= schema.get("maxLength").intValue()
                && Pattern.compile(schema.get("pattern").stringValue()).matcher(value).find();
    }

    private static boolean supportsType(JsonNode schema, String expectedType) {
        for (JsonNode type : schema.get("type")) {
            if (expectedType.equals(type.stringValue())) {
                return true;
            }
        }
        return false;
    }

    private static JsonNode loadTraceIdSchema() throws IOException {
        JsonNode root = JsonMapper.builder().build().readTree(Files.readString(findSchema()));
        return root.get("$defs")
                .get("envelope")
                .get("properties")
                .get("traceId");
    }

    private static Path findSchema() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(SCHEMA_PATH);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到共享 Link Control Event schema");
    }
}
