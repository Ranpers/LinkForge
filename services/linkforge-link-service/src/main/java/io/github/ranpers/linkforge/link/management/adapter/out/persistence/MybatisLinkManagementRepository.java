package io.github.ranpers.linkforge.link.management.adapter.out.persistence;

import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementRepository;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementSnapshot;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisLinkManagementRepository implements LinkManagementRepository {

    private final LinkManagementMapper mapper;

    public MybatisLinkManagementRepository(LinkManagementMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LinkManagementSnapshot find(UUID linkId) {
        LinkManagementRow row = mapper.find(linkId);
        return row == null
                ? null
                : new LinkManagementSnapshot(row.linkId(), row.domainId(), row.createdByUserId());
    }

    @Override
    public boolean updateTarget(UUID linkId, String fullUrl) {
        return mapper.updateTarget(linkId, fullUrl) == 1;
    }

    @Override
    public AvailabilityChangeResult changeAvailability(UUID linkId, boolean enabled) {
        Integer result = mapper.changeAvailability(linkId, enabled);
        return switch (result == null ? 0 : result) {
            case 3 -> AvailabilityChangeResult.CHANGED;
            case 2 -> AvailabilityChangeResult.UNCHANGED;
            case 1 -> AvailabilityChangeResult.FORBIDDEN_STATE;
            default -> AvailabilityChangeResult.NOT_FOUND;
        };
    }

    @Override
    public boolean softDelete(UUID linkId) {
        return mapper.softDelete(linkId) == 1;
    }
}
