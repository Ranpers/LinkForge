package io.github.ranpers.linkforge.iam.security.application.port.in;

import io.github.ranpers.linkforge.iam.security.application.SecurityDispositionDeniedException;
import io.github.ranpers.linkforge.iam.security.application.SecurityTargetUserNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.UserSecurityStatusConflictException;

import java.util.UUID;

/**
 * 对用户执行或解除安全冻结。
 */
public interface ChangeUserSecurityStatusUseCase {

    /**
     * 修改目标用户的安全冻结状态；重复提交当前状态视为成功。
     *
     * @param actorUserId 执行安全处置且必须具备相应权限的用户
     * @param targetUserId 被处置的用户
     * @param suspended {@code true} 表示安全冻结，{@code false} 表示解除冻结
     * @throws SecurityTargetUserNotFoundException 目标用户不存在时
     * @throws UserSecurityStatusConflictException 用户生命周期状态不允许该转换时
     * @throws SecurityDispositionDeniedException 操作者无安全处置权限时
     */
    void change(UUID actorUserId, UUID targetUserId, boolean suspended);
}
