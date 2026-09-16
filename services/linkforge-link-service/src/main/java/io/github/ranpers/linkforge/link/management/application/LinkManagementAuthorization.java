package io.github.ranpers.linkforge.link.management.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LinkManagementAuthorization(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
