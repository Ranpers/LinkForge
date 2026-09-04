package io.github.ranpers.linkforge.iam.security.application.port.in;

import io.github.ranpers.linkforge.iam.security.domain.LinkSecurityRestriction;

import java.util.Objects;
import java.util.UUID;

public record CreateLinkSecurityRestrictionCommand(
        UUID actorUserId,
        UUID targetUserId,
        LinkSecurityRestriction restriction,
        String traceId
) {
    public CreateLinkSecurityRestrictionCommand {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(targetUserId, "targetUserId");
        Objects.requireNonNull(restriction, "restriction");
    }
}
