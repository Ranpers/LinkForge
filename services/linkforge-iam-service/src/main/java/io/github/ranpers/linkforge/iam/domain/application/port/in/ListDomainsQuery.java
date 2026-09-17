package io.github.ranpers.linkforge.iam.domain.application.port.in;

import io.github.ranpers.linkforge.webmvc.validation.InvalidCursorException;
import io.github.ranpers.linkforge.webmvc.validation.InvalidQueryParameterException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 域名列表游标查询；普通用户只读取直接或经角色/分组获授的域名。 */
public record ListDomainsQuery(
        UUID actorUserId,
        boolean canReadAll,
        Boolean enabled,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int pageSize
) {
    public ListDomainsQuery {
        Objects.requireNonNull(actorUserId, "actorUserId");
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw new InvalidCursorException("cursorCreatedAt 与 cursorId 必须同时存在");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new InvalidQueryParameterException("pageSize 必须在 1 到 100 之间");
        }
        searchTerm = searchTerm == null || searchTerm.isBlank() ? null : searchTerm.trim();
    }
}
