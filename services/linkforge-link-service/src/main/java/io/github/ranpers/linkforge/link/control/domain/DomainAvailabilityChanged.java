package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record DomainAvailabilityChanged(
        UUID eventId,
        int schemaVersion,
        String streamKey,
        long revision,
        OffsetDateTime occurredAt,
        ControlEventTraceId traceId,
        UUID domainId,
        String host,
        boolean enabled
) implements LinkControlEvent {

    public DomainAvailabilityChanged {
        Objects.requireNonNull(domainId, "domainId");
        Objects.requireNonNull(host, "host");
        host = host.trim().toLowerCase(Locale.ROOT);
        if (host.isBlank() || host.length() > 253 || host.contains("/")
                || host.contains(":") || host.endsWith(".")) {
            throw new IllegalArgumentException("host 不是规范化域名");
        }
        LinkControlEventInvariant.requireEnvelope(
                eventId, schemaVersion, streamKey, "DOMAIN:" + domainId, revision, occurredAt
        );
    }

    @Override
    public LinkControlEventType eventType() {
        return LinkControlEventType.DOMAIN_AVAILABILITY_CHANGED;
    }

    @Override
    public String partitionKey() {
        return domainId.toString();
    }
}
