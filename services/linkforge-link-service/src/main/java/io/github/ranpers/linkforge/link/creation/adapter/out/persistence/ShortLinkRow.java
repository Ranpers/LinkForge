package io.github.ranpers.linkforge.link.creation.adapter.out.persistence;

import io.github.ranpers.linkforge.link.creation.domain.ShortCodeType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShortLinkRow(
        UUID id,
        UUID createdByUserId,
        UUID groupId,
        String name,
        String linkCode,
        ShortCodeType codeType,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey,
        String requestFingerprint,
        OffsetDateTime createdAt
) {
}
