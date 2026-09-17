package io.github.ranpers.linkforge.iam.user.application.port.in;

import io.github.ranpers.linkforge.webmvc.validation.InvalidCursorException;
import io.github.ranpers.linkforge.webmvc.validation.InvalidQueryParameterException;

import java.time.Instant;
import java.util.UUID;

/** 用户列表游标查询。 */
public record ListUsersQuery(
        UserListStatus status,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int pageSize
) {
    public ListUsersQuery {
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw new InvalidCursorException("cursorCreatedAt 与 cursorId 必须同时存在");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new InvalidQueryParameterException("pageSize 必须在 1 到 100 之间");
        }
        searchTerm = searchTerm == null || searchTerm.isBlank() ? null : searchTerm.trim();
    }
}
