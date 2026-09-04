package io.github.ranpers.linkforge.link.management.application.port.out;

import java.util.UUID;

public interface LinkManagementCache {
    void refreshAfterCommit(UUID linkId);
}
