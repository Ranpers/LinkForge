package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** 所有方法均由同一个投递事务调用;lockDueRows 的行锁必须保持到事务结束。 */
public interface OutboxDispatchStore {

    List<PendingOutboxEvent> lockDueRows(int limit);

    void markSent(UUID eventId);

    void scheduleRetry(UUID eventId, int retryCount, Duration delay, String lastError);

    void park(UUID eventId, int retryCount, String lastError);
}
