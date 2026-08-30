package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantBatchProjectionStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 六条授权变更面的唯一批量执行入口。
 *
 * <p>固定顺序：拓扑锁 → 清空事务缓冲 → 暂存影响范围 → 锁权限流 → 修改绑定
 * → 集合化重算/diff/revision/Outbox → 提交。任何一步失败均回滚。</p>
 */
@Service
public class GrantBatchEngine {

    private final GrantBatchProjectionStore projectionStore;

    public GrantBatchEngine(GrantBatchProjectionStore projectionStore) {
        this.projectionStore = projectionStore;
    }

    @Transactional
    public GrantBatchResult execute(GrantChangePlan plan) {
        plan.topologyLock().acquire();
        projectionStore.resetBatch();

        int affectedPairs = plan.impactStaging().stage();
        if (affectedPairs < 0) {
            throw new IllegalStateException("授权影响范围数量不能为负数");
        }
        if (affectedPairs == 0) {
            plan.bindingMutation().apply();
            return GrantBatchResult.empty();
        }

        projectionStore.prepareAndLock();
        plan.bindingMutation().apply();
        int flippedPairs = projectionStore.reconcileAndAppend(OffsetDateTime.now());
        return new GrantBatchResult(affectedPairs, flippedPairs);
    }
}
