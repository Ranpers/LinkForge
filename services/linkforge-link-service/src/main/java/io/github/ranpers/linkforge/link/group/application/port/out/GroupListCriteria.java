package io.github.ranpers.linkforge.link.group.application.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * 分组列表的持久化查询条件。
 *
 * @param ownerUserId 归属用户过滤，必填；无跨用户读取场景
 * @param cursorCreatedAt 上一页最后一条的创建时间，UTC；首页为 {@code null}
 * @param cursorId 上一页最后一条的标识；首页为 {@code null}
 * @param fetchSize 实际抓取行数，由调用方设为页大小加一
 */
public record GroupListCriteria(
        UUID ownerUserId,
        Instant cursorCreatedAt,
        UUID cursorId,
        int fetchSize
) {
}
