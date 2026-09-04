package io.github.ranpers.linkforge.link.management.adapter.out.persistence;

import java.util.UUID;

public record LinkManagementRow(
        UUID linkId,
        UUID domainId,
        UUID createdByUserId
) {
}
