package io.github.ranpers.linkforge.iam.domain.application.port.in;

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
        // 游标配对与页大小在 Web 边界已经校验：非法游标由 CursorCodec 返回 400，页大小由
        // @Min/@Max 返回 400。能走到这里说明适配器或内部调用存在缺陷，因此抛
        // IllegalArgumentException 交由全局兜底报成 500，不伪装成调用方输入错误。
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw new IllegalArgumentException("cursorCreatedAt 与 cursorId 必须同时存在");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize 必须在 1 到 100 之间");
        }
        searchTerm = searchTerm == null || searchTerm.isBlank() ? null : searchTerm.trim();
    }
}
