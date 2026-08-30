package io.github.ranpers.linkforge.link.availability.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** IAM 下发的一条目标状态事件。 */
public sealed interface AuthorizationEvent permits
        DomainAvailabilityChanged,
        UserAvailabilityChanged,
        UserDomainGrantChanged {

    UUID eventId();

    AuthorizationEventType eventType();

    String streamKey();

    long revision();

    OffsetDateTime occurredAt();

    /** Kafka key；用于在适配器边界校验生产端分区契约。 */
    String partitionKey();
}
