package io.github.ranpers.linkforge.link.creation.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 创建短链所需的框架无关输入。
 *
 * @param actorUserId      发起创建且成为短链所有者的非空用户标识
 * @param groupId          可为空的分组标识；非空时必须属于操作者
 * @param name             去除首尾空白后非空且不超过 64 个字符的展示名称
 * @param shortCodeRequest 自动分配或自定义短码请求，不允许为空
 * @param fullUrl          非空且不超过 2048 个字符的绝对 HTTP(S) 跳转地址
 * @param sortOrder        同组排序值，允许任意 32 位有符号整数
 * @param domainId         承载该短码且已由 IAM 管理的非空域名标识
 * @param expiresAt        可为空的带偏移量过期时刻；非空时必须晚于实际创建时刻
 * @param idempotencyKey   操作者范围内非空且不超过 128 个字符的幂等键；
 *                         相同键必须对应相同请求内容
 */
public record CreateShortLinkCommand(
        UUID actorUserId,
        UUID groupId,
        String name,
        ShortCodeRequest shortCodeRequest,
        String fullUrl,
        int sortOrder,
        UUID domainId,
        OffsetDateTime expiresAt,
        String idempotencyKey
) {
}
