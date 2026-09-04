package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.util.UUID;

/**
 * 对修改或删除短链的请求执行 IAM 权限评估。
 */
public interface ValidateLinkManagementAuthorizationUseCase {

    /**
     * 根据操作者、资源归属和动作生成鉴权决策。
     *
     * <p>业务拒绝通过结果对象表达，不以异常代替拒绝结果。</p>
     *
     * @param actorUserId 发起操作的用户
     * @param domainId 短链所属域名
     * @param createdByUserId 短链创建者
     * @param action 需要执行的管理动作
     * @return 包含原因码和审计标识的完整决策，绝不返回 {@code null}
     */
    LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    );
}
