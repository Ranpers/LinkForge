package io.github.ranpers.linkforge.iam.grant.application.port.out;

import java.util.UUID;

/**
 * 四路授权并集的实时计算(用户直绑 ∨ 用户域名组 ∨ 用户角色直绑 ∨ 用户角色域名组)。
 * 这是授权事实的权威来源,不读投影。
 */
public interface GrantUnionCalculator {

    boolean isGranted(UUID userId, UUID domainId);
}
