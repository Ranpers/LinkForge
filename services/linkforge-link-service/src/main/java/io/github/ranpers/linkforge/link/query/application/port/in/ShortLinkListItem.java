package io.github.ranpers.linkforge.link.query.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** 面向管理列表的短链接只读投影。 */
public record ShortLinkListItem(
        UUID id,
        UUID createdByUserId,
        UUID groupId,
        String name,
        String linkCode,
        String codeType,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        String status,
        String disabledReasonCode,
        Instant expiresAt,
        long revision,
        Instant createdAt,
        Instant updatedAt
) {
}
