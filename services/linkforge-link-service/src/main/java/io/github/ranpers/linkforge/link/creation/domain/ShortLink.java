package io.github.ranpers.linkforge.link.creation.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 保证可持久化短链具备有效标识、公开短码、HTTP(S) 目标和幂等元数据。
 *
 * @param id                 非空的内部 UUIDv7 标识
 * @param createdByUserId    非空的创建者标识，也是幂等键的作用域
 * @param groupId            可为空的分组标识；非空时必须属于创建者
 * @param name               去除首尾空白后非空且不超过 64 个字符的展示名称
 * @param shortCode          非空且已按分配方式完成校验的公开短码
 * @param fullUrl            非空且不超过 2048 个字符的绝对 HTTP(S) URL
 * @param sortOrder          同组排序值，允许任意 32 位有符号整数
 * @param domainId           非空的短链域名标识，也是短码唯一性的作用域
 * @param expiresAt          可为空的带偏移量过期时刻；非空时必须晚于 {@code createdAt}
 * @param idempotencyKey     创建者范围内非空且不超过 128 个字符的幂等键
 * @param requestFingerprint 当前创建请求的 64 位小写 SHA-256 十六进制指纹
 * @param createdAt          非空的带偏移量创建时刻
 */
public record ShortLink(
        UUID id,
        UUID createdByUserId,
        UUID groupId,
        String name,
        ShortCode shortCode,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey,
        String requestFingerprint,
        OffsetDateTime createdAt
) {
    public ShortLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdByUserId, "createdByUserId");
        Objects.requireNonNull(domainId, "domainId");
        Objects.requireNonNull(createdAt, "createdAt");
        name = requireText(name, "name", 64);
        Objects.requireNonNull(shortCode, "shortCode");
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

    public String linkCode() {
        return shortCode.value();
    }

    public ShortCodeType codeType() {
        return shortCode.type();
    }
}
