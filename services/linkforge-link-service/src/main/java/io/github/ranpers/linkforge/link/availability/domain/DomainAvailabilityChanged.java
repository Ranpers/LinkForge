package io.github.ranpers.linkforge.link.availability.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record DomainAvailabilityChanged(
        UUID eventId,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        UUID domainId,
        boolean enabled
) implements AuthorizationEvent {

    public DomainAvailabilityChanged {
        Objects.requireNonNull(domainId, "domainId 不能为空");
        AuthorizationEventInvariant.requireEnvelope(
                eventId,
                streamKey,
                "DOMAIN:" + domainId,
                revision,
                occurredAt
        );
    }

    @Override
    public AuthorizationEventType eventType() {
        return AuthorizationEventType.DOMAIN_AVAILABILITY_CHANGED;
    }

    @Override
    public String partitionKey() {
        return domainId.toString();
    }
}
