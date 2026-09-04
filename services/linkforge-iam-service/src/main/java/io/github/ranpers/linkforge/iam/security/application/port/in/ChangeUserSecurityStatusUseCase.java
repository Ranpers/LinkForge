package io.github.ranpers.linkforge.iam.security.application.port.in;

import java.util.UUID;

public interface ChangeUserSecurityStatusUseCase {
    void change(UUID actorUserId, UUID targetUserId, boolean suspended);
}
