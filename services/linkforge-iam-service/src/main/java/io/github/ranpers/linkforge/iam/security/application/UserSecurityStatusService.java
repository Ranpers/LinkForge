package io.github.ranpers.linkforge.iam.security.application;

import io.github.ranpers.linkforge.iam.security.application.port.in.ChangeUserSecurityStatusUseCase;
import io.github.ranpers.linkforge.iam.security.application.port.out.UserSecurityStatusStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserSecurityStatusService implements ChangeUserSecurityStatusUseCase {

    private final UserSecurityStatusStore store;

    public UserSecurityStatusService(UserSecurityStatusStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public void change(UUID actorUserId, UUID targetUserId, boolean suspended) {
        switch (store.change(actorUserId, targetUserId, suspended)) {
            case CHANGED, UNCHANGED -> {
            }
            case TARGET_NOT_FOUND -> throw new SecurityTargetUserNotFoundException();
            case LIFECYCLE_CONFLICT -> throw new UserSecurityStatusConflictException();
            case DENIED -> throw new SecurityDispositionDeniedException();
        }
    }
}
