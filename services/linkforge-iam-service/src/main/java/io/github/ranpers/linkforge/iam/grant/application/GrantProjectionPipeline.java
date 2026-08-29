package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantOutboxStore;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantProjectionStore;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantUnionCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 授权投影管道(《授权事件契约》§9.2):六条授权变更面唯一的写入口,
 * 调用方(管理用例)不得绕过本管道直接写六张关联表。
 *
 * <p>九步编排,全部处于同一数据库事务:</p>
 * <ol>
 *   <li>先锁定本次关系变更的拓扑锚点:角色相关边锁 t_role,域名组相关边锁 t_domain_group。
 *       叶子绑定/解绑取共享锁,修改锚点的授权扇出取独占锁;这样同类叶子变更仍可并发,
 *       而并发新增两条原本互不相交的边时,至少一方能看见另一方提交后的完整拓扑</li>
 *   <li>事务内由调用方提供的 Supplier 计算受影响 (userId, domainId) 集合——
 *       删除/解绑类变更必须在绑定行消失前取得,晚于此将无法反查受影响者</li>
 *   <li>按 (userId, domainId) 固定排序,并发操作以相同顺序加锁,防死锁</li>
 *   <li>插入缺失的初始行并逐流加锁(FOR UPDATE 锁不住不存在的行)</li>
 *   <li>执行绑定变更动作</li>
 *   <li>实时重算四路授权并集</li>
 *   <li>与投影旧值 diff</li>
 *   <li>翻转才落库:granted 更新 + revision+1 + 同事务写 Outbox;未翻转不产生任何事件</li>
 *   <li>事务提交,绑定/投影/待发事件三者原子生效</li>
 * </ol>
 */
@Service
public class GrantProjectionPipeline {

    private final GrantProjectionStore projectionStore;
    private final GrantUnionCalculator unionCalculator;
    private final GrantOutboxStore outboxStore;

    public GrantProjectionPipeline(
            GrantProjectionStore projectionStore,
            GrantUnionCalculator unionCalculator,
            GrantOutboxStore outboxStore
    ) {
        this.projectionStore = projectionStore;
        this.unionCalculator = unionCalculator;
        this.outboxStore = outboxStore;
    }

    /**
     * @param topologyLock   拓扑锚点锁动作,必须只加数据库行锁且不得提交事务;
     *                       直接用户-域名变更无共享拓扑时也应显式传入空动作
     * @param affectedPairs  受影响集合的来源,在拓扑锁之后、绑定变更之前调用;
     *                       宁可多含(diff 会过滤),不可遗漏
     * @param bindingMutation 绑定表变更动作,只在加锁之后、重算之前执行一次
     */
    @Transactional
    public void project(
            Runnable topologyLock,
            Supplier<Collection<AffectedPair>> affectedPairs,
            Runnable bindingMutation
    ) {
        topologyLock.run();
        List<AffectedPair> ordered = affectedPairs.get().stream()
                .distinct()
                .sorted(Comparator.comparing(AffectedPair::userId).thenComparing(AffectedPair::domainId))
                .toList();

        if (ordered.isEmpty()) {
            bindingMutation.run();
            return;
        }

        Map<AffectedPair, GrantSnapshot> before = projectionStore.lockAndLoad(ordered);
        bindingMutation.run();

        OffsetDateTime occurredAt = OffsetDateTime.now();
        for (AffectedPair pair : ordered) {
            GrantSnapshot old = before.get(pair);
            // lockAndLoad 的端口契约是每个入参对都有快照,缺失说明适配器实现有误,快速失败
            if (old == null) {
                throw new IllegalStateException("投影加锁后缺少快照: " + pair);
            }
            boolean newGranted = unionCalculator.isGranted(pair.userId(), pair.domainId());
            if (old.granted() == newGranted) {
                continue;
            }
            long newRevision = old.revision() + 1;
            projectionStore.saveFlip(pair, newGranted, newRevision);
            outboxStore.appendGrantChanged(pair, newGranted, newRevision, occurredAt);
        }
    }
}
