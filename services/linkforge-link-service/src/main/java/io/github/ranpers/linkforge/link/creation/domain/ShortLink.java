package io.github.ranpers.linkforge.link.creation.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record ShortLink(
        UUID id,
        UUID createdByUserId,
        UUID groupId,
        String name,
        String linkCode,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey,
        String requestFingerprint,
        OffsetDateTime createdAt
) {
    private static final Pattern LINK_CODE = Pattern.compile("[A-Za-z0-9_-]{4,64}");

    public ShortLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdByUserId, "createdByUserId");
        Objects.requireNonNull(domainId, "domainId");
        Objects.requireNonNull(createdAt, "createdAt");
        name = requireText(name, "name", 64);
        linkCode = requireLinkCode(linkCode);
        fullUrl = requireHttpUrl(fullUrl);
        idempotencyKey = requireText(idempotencyKey, "idempotencyKey", 128);
        requestFingerprint = requireFingerprint(requestFingerprint);
        if (expiresAt != null && !expiresAt.isAfter(createdAt)) {
            throw new InvalidShortLinkException("expiresAt 必须晚于创建时间");
        }
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidShortLinkException(field + " 不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidShortLinkException(field + " 长度不能超过 " + maxLength);
        }
        return normalized;
    }

    private static String requireLinkCode(String value) {
        String normalized = requireText(value, "linkCode", 64);
        if (!LINK_CODE.matcher(normalized).matches()) {
            throw new InvalidShortLinkException(
                    "linkCode 只能包含字母、数字、下划线和连字符，长度为 4~64"
            );
        }
        return normalized;
    }

    private static String requireHttpUrl(String value) {
        String normalized = requireText(value, "fullUrl", 2048);
        try {
            URI uri = new URI(normalized);
            if (!("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new InvalidShortLinkException("fullUrl 必须是包含主机名的 HTTP(S) URL");
            }
            return uri.toASCIIString();
        } catch (URISyntaxException exception) {
            throw new InvalidShortLinkException("fullUrl 不是合法 URL", exception);
        }
    }

    private static String requireFingerprint(String value) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new InvalidShortLinkException("requestFingerprint 必须是 SHA-256 十六进制值");
        }
        return value;
    }
}
