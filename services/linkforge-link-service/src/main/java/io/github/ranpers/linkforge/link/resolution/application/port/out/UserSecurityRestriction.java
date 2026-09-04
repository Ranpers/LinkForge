package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSecurityRestriction(
        UUID restrictionId,
        String mode,
        OffsetDateTime rangeStart,
        OffsetDateTime rangeEnd,
        String reasonCode,
        OffsetDateTime createdAt
) {
    public boolean matches(OffsetDateTime linkCreatedAt) {
        return "ALL".equals(mode)
                || "CREATED_DURING".equals(mode)
                && (rangeStart == null || !linkCreatedAt.isBefore(rangeStart))
                && (rangeEnd == null || linkCreatedAt.isBefore(rangeEnd));
    }
}
