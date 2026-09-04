package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.time.OffsetDateTime;

public record LinkResolutionSnapshot(
        String targetUrl,
        String linkStatus,
        OffsetDateTime deletedAt,
        OffsetDateTime expiresAt,
        boolean domainEnabled,
        boolean securityRestricted
) {
}
