package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public sealed interface LinkControlEvent permits
        DomainAvailabilityChanged,
        UserLinkSecurityRestrictionsChanged {

    UUID eventId();

    LinkControlEventType eventType();

    int schemaVersion();

    String streamKey();

    long revision();

    OffsetDateTime occurredAt();

    String traceId();

    String partitionKey();
}
