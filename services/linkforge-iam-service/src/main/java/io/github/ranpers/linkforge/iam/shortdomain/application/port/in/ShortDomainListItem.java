package io.github.ranpers.linkforge.iam.shortdomain.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** 域名管理列表只读投影。 */
public record ShortDomainListItem(
        UUID id,
        String host,
        String name,
        boolean enabled,
        long revision,
        Instant createdAt,
        Instant updatedAt
) {
}
