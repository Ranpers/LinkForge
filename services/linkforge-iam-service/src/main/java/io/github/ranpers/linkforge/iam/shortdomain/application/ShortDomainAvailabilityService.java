package io.github.ranpers.linkforge.iam.shortdomain.application;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ChangeShortDomainAvailabilityUseCase;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainAvailabilityStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShortDomainAvailabilityService implements ChangeShortDomainAvailabilityUseCase {

    private final ShortDomainAvailabilityStore store;

    public ShortDomainAvailabilityService(ShortDomainAvailabilityStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public void change(
            UUID actorUserId,
            UUID shortDomainId,
            boolean enabled,
            ControlEventRequestId requestId
    ) {
        switch (store.change(actorUserId, shortDomainId, enabled, requestId)) {
            case CHANGED, UNCHANGED -> {
            }
            case NOT_FOUND -> throw new ShortDomainNotFoundException();
            case DENIED -> throw new ShortDomainAvailabilityChangeDeniedException();
        }
    }
}
