package io.github.ranpers.linkforge.iam.security.application.port.out;

import java.util.UUID;

/**
 * 原子持久化用户冻结状态及其对应的短链安全限制快照。
 */
public interface UserSecurityStatusStore {

    /**
     * 使用户状态与系统生成的全量短链限制收敛到请求状态。
     *
     * @param actorUserId 具备 {@code security:manage} 权限的非空用户标识
     * @param targetUserId 被处置用户的非空标识
     * @param suspended {@code true} 时确保系统限制存在，{@code false} 时仅撤销系统限制
     * @param traceId 可为空的调用链标识；非空时长度不得超过 64 个字符
     * @return 状态或限制发生变化时返回 {@link ChangeOutcome#CHANGED}；两者均已处于目标状态时返回
     * {@link ChangeOutcome#UNCHANGED}；其余结果表示调用者需要处理的拒绝原因
     */
    ChangeOutcome change(UUID actorUserId, UUID targetUserId, boolean suspended, String traceId);

    enum ChangeOutcome {
        CHANGED,
        UNCHANGED,
        TARGET_NOT_FOUND,
        LIFECYCLE_CONFLICT,
        DENIED
    }
}
