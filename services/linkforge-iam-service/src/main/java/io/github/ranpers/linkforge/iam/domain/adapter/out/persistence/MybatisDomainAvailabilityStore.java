package io.github.ranpers.linkforge.iam.domain.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainAvailabilityStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisDomainAvailabilityStore implements DomainAvailabilityStore {

    private final DomainAvailabilityMapper mapper;

    public MybatisDomainAvailabilityStore(DomainAvailabilityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ChangeResult change(
            UUID actorUserId,
            UUID domainId,
            boolean enabled,
            ControlEventTraceId traceId
    ) {
        Integer result = mapper.change(
                actorUserId,
                domainId,
                enabled,
                traceId == null ? null : traceId.value()
        );
        return switch (result == null ? 0 : result) {
            case 3 -> ChangeResult.CHANGED;
            case 2 -> ChangeResult.UNCHANGED;
            case 1 -> ChangeResult.NOT_FOUND;
            default -> ChangeResult.DENIED;
        };
    }
}
