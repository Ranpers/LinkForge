package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 一次短链管理鉴权的不可变结果。
 *
 * @param allowed 是否允许执行请求的动作
 * @param reasonCode 稳定的机器可读原因码
 * @param decisionId 本次决策的唯一标识，用于跨服务审计与排障
 * @param evaluatedAt 以带偏移量时间表示的决策时刻
 */
public record LinkManagementAuthorization(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
}
