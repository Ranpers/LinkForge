package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record LinkCreationAuthorization(
        UUID userId,
        UUID domainId,
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
    public LinkCreationAuthorization {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(domainId, "domainId");
        Objects.requireNonNull(reasonCode, "reasonCode");
        Objects.requireNonNull(decisionId, "decisionId");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt");
    }
}
