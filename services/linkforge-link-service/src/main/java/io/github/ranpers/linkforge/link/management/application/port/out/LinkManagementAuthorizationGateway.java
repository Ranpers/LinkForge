package io.github.ranpers.linkforge.link.management.application.port.out;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAction;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorization;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorizationUnavailableException;

import java.util.UUID;

/**
 * 通过服务身份向 IAM 请求短链管理决策。
 */
public interface LinkManagementAuthorizationGateway {

    /**
     * 获取操作者针对既有短链执行指定动作的实时决策。
     *
     * @param actorUserId 发起操作的用户
     * @param domainId 短链所属域名
     * @param createdByUserId 短链创建者
     * @param action 请求执行的动作
     * @return IAM 返回的完整决策；业务拒绝通过 {@code allowed=false} 表达
     * @throws LinkManagementAuthorizationUnavailableException 网络、认证或响应协议异常时
     */
    LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    );
}
