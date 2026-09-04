package io.github.ranpers.linkforge.iam.security.application.port.out;

import java.util.UUID;

public interface UserSecurityStatusStore {
    ChangeOutcome change(UUID actorUserId, UUID targetUserId, boolean suspended);

    enum ChangeOutcome {
        CHANGED,
        UNCHANGED,
        TARGET_NOT_FOUND,
        LIFECYCLE_CONFLICT,
        DENIED
    }
}
