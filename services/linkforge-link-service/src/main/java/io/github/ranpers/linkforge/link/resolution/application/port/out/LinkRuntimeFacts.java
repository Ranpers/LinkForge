package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LinkRuntimeFacts(
        UUID linkId,
        UUID domainId,
        UUID createdByUserId,
        String host,
        String linkCode,
        String targetUrl,
        String status,
        OffsetDateTime deletedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        long revision
) {
}
