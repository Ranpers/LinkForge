package io.github.ranpers.linkforge.link.group.application.port.in;

import java.time.Instant;
import java.util.UUID;

/**
 * 分页读取分组的入参。
 *
 * @param ownerUserId 分组归属用户，必须取自访问令牌的 subject；本接口只返回该用户自己的分组
 * @param cursorCreatedAt 上一页最后一条的创建时间，UTC；首页为 {@code null}
 * @param cursorId 上一页最后一条的标识；首页为 {@code null}
 * @param pageSize 期望返回的条数，不含用于探测下一页的额外一条
 */
public record ListGroupsQuery(
        UUID ownerUserId,
        Instant cursorCreatedAt,
        UUID cursorId,
        int pageSize
) {
}
