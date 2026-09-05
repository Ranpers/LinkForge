package io.github.ranpers.linkforge.iam.security.application.port.out;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;

import java.util.UUID;

/**
 * 原子持久化用户短链安全限制、revision 与对应控制事件。
 */
public interface LinkSecurityRestrictionStore {

    /**
     * 创建活动安全限制并发布最新限制快照。
     *
     * @param command 已校验的非空创建命令
     * @return 写入结果及成功时的限制标识
     */
    CreateResult create(CreateLinkSecurityRestrictionCommand command);

    /**
     * 撤销管理员创建的活动限制并发布最新限制快照。
     *
     * @param actorUserId   具备安全处置权限的非空用户标识
     * @param targetUserId  非空的目标用户标识
     * @param restrictionId 非空的目标限制标识
     * @param traceId       可为空；非空时将原值写入控制事件
     * @return 修改、幂等未变、目标不存在或权限拒绝结果
     */
    MutationOutcome revoke(
            UUID actorUserId,
            UUID targetUserId,
            UUID restrictionId,
            ControlEventTraceId traceId
    );

    record CreateResult(MutationOutcome outcome, UUID restrictionId) {
    }

    enum MutationOutcome {
        CHANGED,
        UNCHANGED,
        TARGET_NOT_FOUND,
        RESTRICTION_NOT_FOUND,
        DENIED
    }
}
