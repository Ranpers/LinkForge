package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 创建短链的请求体。
 *
 * @param groupId   可为空的分组标识；非空时必须属于当前主体
 * @param name      去除首尾空白后非空且不超过 64 个字符的展示名称
 * @param linkCode  可为空的自定义短码；为 {@code null} 时由系统分配
 * @param fullUrl   非空且不超过 2048 个字符的绝对 HTTP(S) 跳转地址
 * @param sortOrder 同组展示排序值，允许任意 32 位有符号整数；为 {@code null} 时按 0 处理
 * @param domainId  承载该短码且已由 IAM 管理的非空域名标识
 * @param expiresAt 可为空的带偏移量过期时刻；非空时必须晚于实际创建时刻
 */
public record CreateShortLinkRequest(
        UUID groupId,
        @NotBlank @Size(max = 64) String name,
        String linkCode,
        @NotBlank @Size(max = 2048) String fullUrl,
        Integer sortOrder,
        @NotNull UUID domainId,
        OffsetDateTime expiresAt
) {
}
