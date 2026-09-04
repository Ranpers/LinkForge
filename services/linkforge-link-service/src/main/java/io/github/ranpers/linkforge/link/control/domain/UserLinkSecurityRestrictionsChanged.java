package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserLinkSecurityRestrictionsChanged(
        UUID eventId,
        int schemaVersion,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        String traceId,
        UUID userId,
        List<LinkSecurityRestriction> restrictions
) implements LinkControlEvent {

    public UserLinkSecurityRestrictionsChanged {
        Objects.requireNonNull(userId, "userId");
        restrictions = List.copyOf(restrictions);
        LinkControlEventInvariant.requireEnvelope(
                eventId,
                schemaVersion,
                streamKey,
                "USER_LINK_SECURITY:" + userId,
                revision,
                occurredAt
        );
    }

    @Override
    public LinkControlEventType eventType() {
        return LinkControlEventType.USER_LINK_SECURITY_RESTRICTIONS_CHANGED;
    }

    @Override
    public String partitionKey() {
        return userId.toString();
    }
}
