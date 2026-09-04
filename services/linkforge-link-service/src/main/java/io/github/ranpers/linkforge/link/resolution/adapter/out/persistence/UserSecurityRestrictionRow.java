package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSecurityRestrictionRow(
        UUID restrictionId,
        String mode,
        OffsetDateTime rangeStart,
        OffsetDateTime rangeEnd,
        String reasonCode,
        OffsetDateTime createdAt
) {
}
