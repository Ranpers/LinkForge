package io.github.ranpers.linkforge.iam.domain.application.port.in;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.domain.application.DomainAvailabilityChangeDeniedException;
import io.github.ranpers.linkforge.iam.domain.application.DomainNotFoundException;

import java.util.UUID;

/**
 * 修改短链域名的运行时可用状态。
 */
public interface ChangeDomainAvailabilityUseCase {

    /**
     * 将域名切换到指定状态；重复提交当前状态视为成功。
     *
     * @param actorUserId 发起操作且必须拥有域名管理权限的用户
     * @param domainId    要修改的域名
     * @param enabled     {@code true} 表示允许该域名继续解析短链
     * @param traceId     可为空；非空时包含 1 至 64 个字符且随控制事件传播
     * @throws DomainNotFoundException                 域名不存在时
     * @throws DomainAvailabilityChangeDeniedException 操作者无权管理该域名时
     */
    void change(
            UUID actorUserId,
            UUID domainId,
            boolean enabled,
            ControlEventTraceId traceId
    );
}
