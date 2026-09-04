package io.github.ranpers.linkforge.link.management.application.port.in;

import java.util.UUID;

public interface ManageShortLinkUseCase {
    void updateTarget(UUID actorUserId, UUID linkId, String fullUrl);

    void changeAvailability(UUID actorUserId, UUID linkId, boolean enabled);

    void delete(UUID actorUserId, UUID linkId);
}
