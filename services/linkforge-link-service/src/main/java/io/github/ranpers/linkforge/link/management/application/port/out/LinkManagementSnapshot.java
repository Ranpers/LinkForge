package io.github.ranpers.linkforge.link.management.application.port.out;

import java.util.UUID;

public record LinkManagementSnapshot(
        UUID linkId,
        UUID shortDomainId,
        UUID createdByUserId
) {
}
