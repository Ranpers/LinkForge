package io.github.ranpers.linkforge.link.creation.adapter.out.iam;

import java.time.OffsetDateTime;
import java.util.UUID;

record IamLinkCreationAuthorizationResponse(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
