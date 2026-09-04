package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.util.UUID;

/**
 * 对短链创建请求执行 IAM 权限评估。
 */
public interface ValidateLinkCreationAuthorizationUseCase {

    /**
     * 评估用户能否在指定域名下创建短链。
     *
     * <p>业务拒绝通过结果对象表达，不以异常代替拒绝结果。</p>
     *
     * @param userId 需要评估的用户
     * @param domainId 目标短链域名
     * @return 包含原因码和审计标识的完整决策，绝不返回 {@code null}
     */
    LinkCreationAuthorization validate(UUID userId, UUID domainId);
}
