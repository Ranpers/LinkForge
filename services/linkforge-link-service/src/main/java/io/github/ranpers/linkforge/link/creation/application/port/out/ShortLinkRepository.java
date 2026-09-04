package io.github.ranpers.linkforge.link.creation.application.port.out;

import io.github.ranpers.linkforge.link.creation.domain.ShortLink;

import java.util.Optional;
import java.util.UUID;

public interface ShortLinkRepository {

    Optional<ShortLink> findByIdempotencyKey(UUID userId, String idempotencyKey);

    boolean groupBelongsToUser(UUID groupId, UUID userId);

    /** @return true when inserted, false when the idempotency constraint already exists. */
    boolean insertIfIdempotencyAbsent(ShortLink link);
}
