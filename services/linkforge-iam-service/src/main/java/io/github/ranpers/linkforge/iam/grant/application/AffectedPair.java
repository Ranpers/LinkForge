package io.github.ranpers.linkforge.iam.grant.application;

import java.util.UUID;

/**
 * 一条授权状态流:用户×域名。作为投影加锁与 diff 的键,值为语义。
 */
public record AffectedPair(UUID userId, UUID domainId) {
}
