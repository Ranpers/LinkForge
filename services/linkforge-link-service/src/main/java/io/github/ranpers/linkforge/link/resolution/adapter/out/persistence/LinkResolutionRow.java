package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import java.time.OffsetDateTime;

public record LinkResolutionRow(
        java.util.UUID linkId,
        java.util.UUID domainId,
        java.util.UUID createdByUserId,
        String host,
        String linkCode,
        String targetUrl,
        String linkStatus,
        OffsetDateTime deletedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        long revision
) {
}
