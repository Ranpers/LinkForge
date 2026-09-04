package io.github.ranpers.linkforge.iam.domain.application.port.in;

import java.util.UUID;

public interface ChangeDomainAvailabilityUseCase {
    void change(UUID actorUserId, UUID domainId, boolean enabled, String traceId);
}
