package io.github.ranpers.linkforge.link.management.application.port.out;

import java.util.UUID;

public interface LinkManagementRepository {
    LinkManagementSnapshot find(UUID linkId);

    boolean updateTarget(UUID linkId, String fullUrl);

    AvailabilityChangeResult changeAvailability(UUID linkId, boolean enabled);

    boolean softDelete(UUID linkId);

    enum AvailabilityChangeResult {
        CHANGED,
        UNCHANGED,
        FORBIDDEN_STATE,
        NOT_FOUND
    }
}
