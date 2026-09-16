package io.github.ranpers.linkforge.link.query.application.port.in;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 查询短链接列表的输入；非全量读取者的创建人筛选会被强制收敛为操作者本人。
 */
public record ListShortLinksQuery(
        UUID actorUserId,
        boolean canReadAll,
        UUID createdByUserId,
        UUID domainId,
        ShortLinkListStatus status,
        String searchTerm,
        Instant cursorCreatedAt,
        UUID cursorId,
        int pageSize
) {
    public ListShortLinksQuery {
        Objects.requireNonNull(actorUserId, "actorUserId");
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw new IllegalArgumentException("cursorCreatedAt 与 cursorId 必须同时存在");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize 必须在 1 到 100 之间");
        }
        searchTerm = normalize(searchTerm);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
