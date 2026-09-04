package io.github.ranpers.linkforge.iam.security.application.port.in;

import java.util.UUID;

public interface ManageLinkSecurityRestrictionUseCase {
    UUID create(CreateLinkSecurityRestrictionCommand command);

    void revoke(UUID actorUserId, UUID targetUserId, UUID restrictionId, String traceId);
}
