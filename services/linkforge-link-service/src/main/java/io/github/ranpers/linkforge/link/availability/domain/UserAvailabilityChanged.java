package io.github.ranpers.linkforge.link.availability.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record UserAvailabilityChanged(
        UUID eventId,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        UUID userId,
        boolean enabled
) implements AuthorizationEvent {

    public UserAvailabilityChanged {
        Objects.requireNonNull(userId, "userId 不能为空");
        AuthorizationEventInvariant.requireEnvelope(
                eventId,
                streamKey,
                "USER:" + userId,
                revision,
                occurredAt
        );
    }

    @Override
    public AuthorizationEventType eventType() {
        return AuthorizationEventType.USER_AVAILABILITY_CHANGED;
    }

    @Override
    public String partitionKey() {
        return userId.toString();
    }
}
