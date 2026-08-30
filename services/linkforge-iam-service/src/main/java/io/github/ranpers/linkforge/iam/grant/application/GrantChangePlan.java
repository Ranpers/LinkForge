package io.github.ranpers.linkforge.iam.grant.application;

import java.util.Objects;

/**
 * 一次授权关系变更的最小描述。十二个绑定/解绑用例只负责提供这三项，
 * 批量重算、翻转、版本和 Outbox 由 {@link GrantBatchEngine} 统一处理。
 */
public record GrantChangePlan(
        TopologyLock topologyLock,
        ImpactStaging impactStaging,
        BindingMutation bindingMutation
) {

    public GrantChangePlan {
        Objects.requireNonNull(topologyLock, "topologyLock");
        Objects.requireNonNull(impactStaging, "impactStaging");
        Objects.requireNonNull(bindingMutation, "bindingMutation");
    }

    @FunctionalInterface
    public interface TopologyLock {
        void acquire();
    }

    /**
     * 本事务暂存的唯一 (userId, domainId) 数量。
     * */
    @FunctionalInterface
    public interface ImpactStaging {
        int stage();
    }

    @FunctionalInterface
    public interface BindingMutation {
        void apply();
    }
}
