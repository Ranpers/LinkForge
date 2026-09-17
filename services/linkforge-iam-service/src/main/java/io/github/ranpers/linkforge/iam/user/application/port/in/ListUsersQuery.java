package io.github.ranpers.linkforge.iam.user.application.port.in;

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
