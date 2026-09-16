package io.github.ranpers.linkforge.link.creation.application;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record LinkCreationAuthorization(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
    public LinkCreationAuthorization {
        Objects.requireNonNull(reasonCode, "reasonCode");
        Objects.requireNonNull(decisionId, "decisionId");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt");
    }
}
