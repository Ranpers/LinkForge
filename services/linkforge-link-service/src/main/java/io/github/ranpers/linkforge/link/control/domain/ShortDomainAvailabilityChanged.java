package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record ShortDomainAvailabilityChanged(
        UUID eventId,
        int schemaVersion,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        ControlEventRequestId requestId,
        UUID shortDomainId,
        String host,
        boolean enabled
) implements LinkControlEvent {

    public ShortDomainAvailabilityChanged {
        Objects.requireNonNull(shortDomainId, "shortDomainId");
        Objects.requireNonNull(host, "host");
        host = host.trim().toLowerCase(Locale.ROOT);
        if (host.isBlank() || host.length() > 253 || host.contains("/")
                || host.contains(":") || host.endsWith(".")) {
            throw new IllegalArgumentException("host 不是规范化域名");
        }
        LinkControlEventInvariant.requireEnvelope(
                eventId, schemaVersion, streamKey, "SHORT_DOMAIN:" + shortDomainId, revision, occurredAt
        );
    }

    @Override
    public LinkControlEventType eventType() {
        return LinkControlEventType.SHORT_DOMAIN_AVAILABILITY_CHANGED;
    }

    @Override
    public String partitionKey() {
        return shortDomainId.toString();
    }
}
