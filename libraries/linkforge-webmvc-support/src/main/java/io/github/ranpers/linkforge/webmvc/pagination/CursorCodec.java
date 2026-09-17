package io.github.ranpers.linkforge.webmvc.pagination;

import io.github.ranpers.linkforge.webmvc.validation.InvalidCursorException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/** 将稳定排序键编码为 API 不透明游标，并统一拒绝畸形输入。 */
public final class CursorCodec {

    private static final int MAX_ENCODED_LENGTH = 256;

    private CursorCodec() {
    }

    public static CursorPosition decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new CursorPosition(null, null);
        }
        if (cursor.length() > MAX_ENCODED_LENGTH) {
            throw invalidCursor();
        }
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 2) {
                throw invalidCursor();
            }
            return new CursorPosition(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (IllegalArgumentException exception) {
            throw invalidCursor();
        }
    }

    public static String encode(Instant createdAt, UUID id) {
        String value = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static InvalidCursorException invalidCursor() {
        return new InvalidCursorException("cursor 非法");
    }

    public record CursorPosition(Instant createdAt, UUID id) {
    }
}
