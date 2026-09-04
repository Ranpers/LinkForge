package io.github.ranpers.linkforge.link.creation.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateShortLinkCommand(
        UUID actorUserId,
        UUID groupId,
        String name,
        String linkCode,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey
) {
}
