package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 一次短链创建鉴权的不可变结果。
 *
 * @param userId 被评估的用户
 * @param domainId 被评估的域名
 * @param allowed 是否允许创建
 * @param reasonCode 稳定的机器可读原因码，允许与拒绝均必须提供
 * @param decisionId 本次决策的唯一标识，用于跨服务审计与排障
 * @param evaluatedAt 以带偏移量时间表示的决策时刻
 */
public record LinkCreationAuthorization(
        UUID userId,
        UUID domainId,
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
    public LinkCreationAuthorization {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(domainId, "domainId");
        Objects.requireNonNull(reasonCode, "reasonCode");
        Objects.requireNonNull(decisionId, "decisionId");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt");
    }
}
