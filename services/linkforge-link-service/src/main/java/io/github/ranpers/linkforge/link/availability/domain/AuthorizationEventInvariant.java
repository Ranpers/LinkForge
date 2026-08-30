package io.github.ranpers.linkforge.link.availability.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

final class AuthorizationEventInvariant {

    private AuthorizationEventInvariant() {
    }

    static void requireEnvelope(
            UUID eventId,
            String streamKey,
            String expectedStreamKey,
            long revision,
            OffsetDateTime occurredAt
    ) {
        Objects.requireNonNull(eventId, "eventId 不能为空");
        Objects.requireNonNull(occurredAt, "occurredAt 不能为空");
        if (revision < 1) {
            throw new IllegalArgumentException("revision 必须大于 0");
        }
        if (!expectedStreamKey.equals(streamKey)) {
            throw new IllegalArgumentException(
                    "streamKey 与事件标识不一致: expected=" + expectedStreamKey + ", actual=" + streamKey
            );
        }
    }
}
