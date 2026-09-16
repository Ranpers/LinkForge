package io.github.ranpers.linkforge.iam.domain.application.port.in;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;

import java.util.UUID;

/**
 * 创建域名的入参。
 *
 * @param actorUserId 发起操作的已认证用户，权限校验以其当前角色权限为准
 * @param host        原始主机名，由 {@link io.github.ranpers.linkforge.iam.domain.domain.DomainHost} 归一化
 * @param name        可为空的展示名称，不参与解析
 * @param traceId     可为空；非空时将原值写入本次操作产生的控制事件
 */
public record CreateDomainCommand(
        UUID actorUserId,
        String host,
        String name,
        ControlEventTraceId traceId
) {
}
