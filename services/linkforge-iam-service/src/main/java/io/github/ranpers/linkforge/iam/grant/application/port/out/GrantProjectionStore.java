package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.GrantSnapshot;

import java.util.List;
import java.util.Map;

/**
 * t_user_domain_grant_state 的投影存储。实现必须保证:
 * 对不存在的流先插入初始行(granted=false, revision=0)再加锁——FOR UPDATE 锁不住不存在的行。
 */
public interface GrantProjectionStore {

    /**
     * 按给定顺序(调用方已按 userId,domainId 排序)加行锁并返回加锁时刻快照。
     * 锁持有至当前事务结束。
     */
    Map<AffectedPair, GrantSnapshot> lockAndLoad(List<AffectedPair> orderedPairs);

    /** 翻转落库:仅由管道在检测到 granted 翻转时调用,revision 已加一。 */
    void saveFlip(AffectedPair pair, boolean granted, long revision);
}
