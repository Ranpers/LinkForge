package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

final class LinkControlEventInvariant {

    private LinkControlEventInvariant() {
    }

    static void requireEnvelope(
            UUID eventId,
            int schemaVersion,
            String streamKey,
            String expectedStreamKey,
            long revision,
            OffsetDateTime occurredAt
    ) {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (schemaVersion != 1) {
            throw new IllegalArgumentException("仅支持 schemaVersion=1");
        }
        if (revision < 1) {
            throw new IllegalArgumentException("revision 必须大于 0");
        }
        if (!expectedStreamKey.equals(streamKey)) {
            throw new IllegalArgumentException(
                    "streamKey 与事件主体不一致: expected=" + expectedStreamKey + ", actual=" + streamKey
            );
        }
    }
}
