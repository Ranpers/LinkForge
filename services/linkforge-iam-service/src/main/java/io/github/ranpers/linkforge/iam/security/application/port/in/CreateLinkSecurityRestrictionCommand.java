package io.github.ranpers.linkforge.iam.security.application.port.in;

import io.github.ranpers.linkforge.iam.security.domain.LinkSecurityRestriction;

import java.util.Objects;
import java.util.UUID;

/**
 * 创建用户短链安全限制所需的完整上下文。
 *
 * @param actorUserId 执行安全处置的用户
 * @param targetUserId 限制所作用的短链创建者
 * @param restriction 已校验时间范围不变量的限制规则
 * @param traceId 可为空的调用链标识，随控制事件传播
 */
public record CreateLinkSecurityRestrictionCommand(
        UUID actorUserId,
        UUID targetUserId,
        LinkSecurityRestriction restriction,
        String traceId
) {
    public CreateLinkSecurityRestrictionCommand {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(targetUserId, "targetUserId");
        Objects.requireNonNull(restriction, "restriction");
    }
}
