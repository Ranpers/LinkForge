package io.github.ranpers.linkforge.iam.domain.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** 域名管理列表只读投影。 */
public record DomainListItem(
        UUID id,
        String domain,
        String name,
        boolean enabled,
        long revision,
        Instant createdAt,
        Instant updatedAt
) {
}
