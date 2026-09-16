package io.github.ranpers.linkforge.iam.user.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** 用户列表持久化查询条件。 */
public record UserListCriteria(
        Integer status,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int fetchSize
) {
}
