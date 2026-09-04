package io.github.ranpers.linkforge.link.creation.application.port.out;

import io.github.ranpers.linkforge.link.creation.application.IamAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.creation.application.LinkCreationAuthorization;

import java.util.UUID;

/**
 * 通过服务身份向 IAM 请求短链创建决策。
 */
public interface IamAuthorizationGateway {

    /**
     * 获取用户在指定域名下创建短链的实时决策。
     *
     * @param userId 发起创建的用户
     * @param domainId 目标短链域名
     * @return IAM 返回的完整决策；业务拒绝通过 {@code allowed=false} 表达
     * @throws IamAuthorizationUnavailableException 网络、认证或响应协议异常时
     */
    LinkCreationAuthorization validate(UUID userId, UUID domainId);
}
