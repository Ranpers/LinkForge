package io.github.ranpers.linkforge.iam.security.application.port.in;

import io.github.ranpers.linkforge.iam.security.application.SecurityDispositionDeniedException;
import io.github.ranpers.linkforge.iam.security.application.SecurityTargetUserNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.UserSecurityStatusConflictException;

import java.util.UUID;

/**
 * 保证用户安全冻结状态与其既有短链的系统限制保持一致。
 */
public interface ChangeUserSecurityStatusUseCase {

    /**
     * 修改目标用户的安全冻结状态；冻结会暂停其全部既有短链，解除冻结仅撤销由本状态创建的限制。
     *
     * <p>重复提交当前状态视为成功。管理员单独创建的安全限制不会因解除用户冻结而被撤销。
     *
     * @param actorUserId 执行安全处置且必须具备 {@code security:manage} 权限的非空用户标识
     * @param targetUserId 被处置用户的非空标识，也是短链安全限制事件的关联标识
     * @param suspended {@code true} 表示安全冻结，{@code false} 表示解除冻结
     * @param traceId 可为空的调用链标识；非空时长度不得超过 64 个字符
     * @throws SecurityTargetUserNotFoundException 目标用户不存在时
     * @throws UserSecurityStatusConflictException 用户生命周期状态不允许该转换时
     * @throws SecurityDispositionDeniedException 操作者无安全处置权限时
     * @implNote 用户状态、系统安全限制、revision 与 Outbox 快照在同一数据库事务中更新；
     * 快照由消息链路最终同步到短链服务，因此冻结对解析链路的生效存在传播延迟
     */
    void change(UUID actorUserId, UUID targetUserId, boolean suspended, String traceId);
}
