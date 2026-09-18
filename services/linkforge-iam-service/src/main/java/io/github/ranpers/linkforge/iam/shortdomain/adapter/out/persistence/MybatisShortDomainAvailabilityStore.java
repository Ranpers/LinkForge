package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainAvailabilityStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisShortDomainAvailabilityStore implements ShortDomainAvailabilityStore {

    private final ShortDomainAvailabilityMapper mapper;

    public MybatisShortDomainAvailabilityStore(ShortDomainAvailabilityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ChangeResult change(
            UUID actorUserId,
            UUID shortDomainId,
            boolean enabled,
            ControlEventRequestId requestId
    ) {
        Integer result = mapper.change(
                actorUserId,
                shortDomainId,
                enabled,
                requestId == null ? null : requestId.value()
        );
        return switch (result == null ? 0 : result) {
            case 3 -> ChangeResult.CHANGED;
            case 2 -> ChangeResult.UNCHANGED;
            case 1 -> ChangeResult.NOT_FOUND;
            default -> ChangeResult.DENIED;
        };
    }
}
