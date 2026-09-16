package io.github.ranpers.linkforge.link.management.adapter.out.iam;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IamLinkManagementAuthorizationResponse(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
