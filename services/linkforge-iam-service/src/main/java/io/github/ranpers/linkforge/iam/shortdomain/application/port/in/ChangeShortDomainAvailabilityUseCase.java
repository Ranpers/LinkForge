package io.github.ranpers.linkforge.iam.shortdomain.application.port.in;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainAvailabilityChangeDeniedException;
import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainNotFoundException;

import java.util.UUID;

/**
 * 修改短链域名的运行时可用状态。
 */
public interface ChangeShortDomainAvailabilityUseCase {

    /**
     * 将域名切换到指定状态；重复提交当前状态视为成功。
     *
     * @param actorUserId 发起操作且必须拥有域名管理权限的用户
     * @param shortDomainId    要修改的域名
     * @param enabled     {@code true} 表示允许该域名继续解析短链
     * @param requestId     可为空；非空时是规范 UUID，随控制事件传播
     * @throws ShortDomainNotFoundException                 域名不存在时
     * @throws ShortDomainAvailabilityChangeDeniedException 操作者无权管理该域名时
     */
    void change(
            UUID actorUserId,
            UUID shortDomainId,
            boolean enabled,
            ControlEventRequestId requestId
    );
}
