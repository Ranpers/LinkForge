package io.github.ranpers.linkforge.iam.security.application.port.out;

import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;

import java.util.UUID;

public interface LinkSecurityRestrictionStore {
    CreateResult create(CreateLinkSecurityRestrictionCommand command);

    MutationOutcome revoke(
            UUID actorUserId,
            UUID targetUserId,
            UUID restrictionId,
            String traceId
    );

    record CreateResult(MutationOutcome outcome, UUID restrictionId) {
    }

    enum MutationOutcome {
        CHANGED,
        UNCHANGED,
        TARGET_NOT_FOUND,
        RESTRICTION_NOT_FOUND,
        DENIED
    }
}
