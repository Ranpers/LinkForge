package io.github.ranpers.linkforge.iam.domain.application.port.out;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;

import java.util.UUID;

/**
 * 原子修改域名状态并追加相同 revision 的控制事件。
 */
public interface DomainAvailabilityStore {

    /**
     * 将域名状态收敛到请求值。
     *
     * @param actorUserId 具备域名管理权限的非空用户标识
     * @param domainId    非空的目标域名标识
     * @param enabled     {@code true} 表示启用，{@code false} 表示停用
     * @param traceId     可为空；非空时将原值写入控制事件
     * @return 修改结果，包括幂等未变、目标不存在和权限拒绝
     */
    ChangeResult change(
            UUID actorUserId,
            UUID domainId,
            boolean enabled,
            ControlEventTraceId traceId
    );

    enum ChangeResult {
        CHANGED,
        UNCHANGED,
        NOT_FOUND,
        DENIED
    }
}
