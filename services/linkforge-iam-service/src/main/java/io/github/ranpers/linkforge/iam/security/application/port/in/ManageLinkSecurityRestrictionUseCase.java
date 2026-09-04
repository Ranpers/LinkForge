package io.github.ranpers.linkforge.iam.security.application.port.in;

import io.github.ranpers.linkforge.iam.security.application.LinkSecurityRestrictionNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.SecurityDispositionDeniedException;
import io.github.ranpers.linkforge.iam.security.application.SecurityTargetUserNotFoundException;

import java.util.UUID;

/**
 * 管理作用于用户既有短链的安全限制规则。
 */
public interface ManageLinkSecurityRestrictionUseCase {

    /**
     * 创建并立即启用一条限制规则。
     *
     * @param command 操作者、目标用户、规则和追踪信息
     * @return 新限制规则的唯一标识
     * @throws SecurityTargetUserNotFoundException 目标用户不存在时
     * @throws SecurityDispositionDeniedException 操作者无安全处置权限时
     */
    UUID create(CreateLinkSecurityRestrictionCommand command);

    /**
     * 撤销一条活动限制；重复撤销视为成功。
     *
     * @param actorUserId 执行安全处置的用户
     * @param targetUserId 限制所作用的短链创建者
     * @param restrictionId 要撤销的限制
     * @param traceId 可为空的调用链标识，随控制事件传播
     * @throws SecurityTargetUserNotFoundException 目标用户不存在时
     * @throws LinkSecurityRestrictionNotFoundException 限制不存在或不属于目标用户时
     * @throws SecurityDispositionDeniedException 操作者无安全处置权限时
     */
    void revoke(UUID actorUserId, UUID targetUserId, UUID restrictionId, String traceId);
}
