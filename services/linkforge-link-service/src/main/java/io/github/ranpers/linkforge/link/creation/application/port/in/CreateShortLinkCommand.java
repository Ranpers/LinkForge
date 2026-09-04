package io.github.ranpers.linkforge.link.creation.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 创建短链所需的框架无关输入。
 *
 * @param actorUserId 发起创建的用户，也是新短链的所有者
 * @param groupId 可为空的分组；非空时必须属于操作者
 * @param name 供管理端展示的名称
 * @param linkCode 目标域名内唯一的短码
 * @param fullUrl 跳转目标的绝对 HTTP 或 HTTPS URL
 * @param sortOrder 同组内的排序值
 * @param domainId 承载该短码的域名
 * @param expiresAt 可为空的带偏移量过期时刻，必须晚于创建时刻
 * @param idempotencyKey 操作者范围内的幂等键；相同键必须对应相同请求内容
 */
public record CreateShortLinkCommand(
        UUID actorUserId,
        UUID groupId,
        String name,
        String linkCode,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey
) {
}
