package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LinkManagementAuthorization(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
