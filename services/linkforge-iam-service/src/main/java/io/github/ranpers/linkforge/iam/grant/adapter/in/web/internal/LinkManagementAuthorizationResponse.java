package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LinkManagementAuthorizationResponse(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
