package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 数据库集合化授权投影端口。
 *
 * <p>实现以当前事务为边界暂存受影响对；prepareAndLock 必须为缺失流创建
 * false/revision=0 占位并按固定顺序加锁；reconcileAndAppend 必须批量计算四路并集，
 * 只更新翻转行并在同一数据库语句中写入对应 Outbox。</p>
 */
public interface GrantBatchProjectionStore {

    void resetBatch();

    /** 供单用户/单域名等小范围用例使用；大型影响范围应由适配器用 INSERT ... SELECT 暂存。 */
    int stagePairs(List<AffectedPair> pairs);

    void prepareAndLock();

    /** @return 真正发生 granted 翻转并写入 Outbox 的行数。 */
    int reconcileAndAppend(OffsetDateTime occurredAt);
}
