package io.github.ranpers.linkforge.iam.domain.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** 域名列表持久化查询条件。 */
public record DomainListCriteria(
        UUID actorUserId,
        boolean canReadAll,
        Boolean enabled,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int fetchSize
) {
}
