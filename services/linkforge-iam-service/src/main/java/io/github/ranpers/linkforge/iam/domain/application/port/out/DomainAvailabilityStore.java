package io.github.ranpers.linkforge.iam.domain.application.port.out;

import java.util.UUID;

public interface DomainAvailabilityStore {
    ChangeResult change(UUID actorUserId, UUID domainId, boolean enabled, String traceId);

    enum ChangeResult {
        CHANGED,
        UNCHANGED,
        NOT_FOUND,
        DENIED
    }
}
