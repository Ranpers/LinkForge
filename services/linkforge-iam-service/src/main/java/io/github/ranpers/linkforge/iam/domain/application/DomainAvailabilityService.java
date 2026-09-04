package io.github.ranpers.linkforge.iam.domain.application;

import io.github.ranpers.linkforge.iam.domain.application.port.in.ChangeDomainAvailabilityUseCase;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainAvailabilityStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DomainAvailabilityService implements ChangeDomainAvailabilityUseCase {

    private final DomainAvailabilityStore store;

    public DomainAvailabilityService(DomainAvailabilityStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public void change(UUID actorUserId, UUID domainId, boolean enabled, String traceId) {
        switch (store.change(actorUserId, domainId, enabled, traceId)) {
            case CHANGED, UNCHANGED -> {
            }
            case NOT_FOUND -> throw new DomainNotFoundException();
            case DENIED -> throw new DomainAvailabilityChangeDeniedException();
        }
    }
}
