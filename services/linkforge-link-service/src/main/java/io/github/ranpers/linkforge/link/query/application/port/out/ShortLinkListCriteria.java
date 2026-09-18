package io.github.ranpers.linkforge.link.query.application.port.out;

import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListStatus;

import java.time.Instant;
import java.util.UUID;

/** 短链接列表持久化查询条件。 */
public record ShortLinkListCriteria(
        UUID createdByUserId,
        UUID shortDomainId,
        ShortLinkListStatus status,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int fetchSize
) {
}
