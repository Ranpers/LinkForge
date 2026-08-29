package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;

import java.time.OffsetDateTime;

/**
 * 事务性 Outbox:事件行与业务变更同事务落库,由投递器异步发往 Kafka。
 * 事件形态、streamKey 与分区键由适配器按《LinkForge 授权事件契约》构造。
 */
public interface GrantOutboxStore {

    /** UserDomainGrantChanged:携带目标状态,翻转时由管道调用。 */
    void appendGrantChanged(AffectedPair pair, boolean granted, long revision, OffsetDateTime occurredAt);
}
