package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 保证控制事件信封具有可用于幂等和顺序处理的稳定元数据。
 */
public sealed interface LinkControlEvent permits
        DomainAvailabilityChanged,
        UserLinkSecurityRestrictionsChanged {

    UUID eventId();

    LinkControlEventType eventType();

    int schemaVersion();

    String streamKey();

    long revision();

    @SuppressWarnings("unused")
    OffsetDateTime occurredAt();

    ControlEventTraceId traceId();

    String partitionKey();
}
