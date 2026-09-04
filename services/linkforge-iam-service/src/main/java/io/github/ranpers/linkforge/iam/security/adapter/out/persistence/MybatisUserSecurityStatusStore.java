package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.security.application.port.out.UserSecurityStatusStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisUserSecurityStatusStore implements UserSecurityStatusStore {

    private final UserSecurityStatusMapper mapper;

    public MybatisUserSecurityStatusStore(UserSecurityStatusMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ChangeOutcome change(UUID actorUserId, UUID targetUserId, boolean suspended) {
        Integer result = mapper.change(actorUserId, targetUserId, suspended);
        return switch (result == null ? 0 : result) {
            case 4 -> ChangeOutcome.CHANGED;
            case 3 -> ChangeOutcome.UNCHANGED;
            case 2 -> ChangeOutcome.LIFECYCLE_CONFLICT;
            case 1 -> ChangeOutcome.TARGET_NOT_FOUND;
            default -> ChangeOutcome.DENIED;
        };
    }
}
