package io.github.ranpers.linkforge.link.availability.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record UserDomainGrantChanged(
        UUID eventId,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        UUID userId,
        UUID domainId,
        boolean granted
) implements AuthorizationEvent {

    public UserDomainGrantChanged {
        Objects.requireNonNull(userId, "userId 不能为空");
        Objects.requireNonNull(domainId, "domainId 不能为空");
        AuthorizationEventInvariant.requireEnvelope(
                eventId,
                streamKey,
                "USER_DOMAIN:" + userId + ":" + domainId,
                revision,
                occurredAt
        );
    }

    @Override
    public AuthorizationEventType eventType() {
        return AuthorizationEventType.USER_DOMAIN_GRANT_CHANGED;
    }

    @Override
    public String partitionKey() {
        return domainId.toString();
    }
}
