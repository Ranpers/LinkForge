package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;
import io.github.ranpers.linkforge.iam.grant.application.port.out.OutboxDispatchStore;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
public class MybatisOutboxDispatchStore implements OutboxDispatchStore {

    private final OutboxDispatchMapper mapper;

    public MybatisOutboxDispatchStore(OutboxDispatchMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PendingOutboxEvent> lockDueRows(int limit) {
        return mapper.lockDueRows(limit).stream()
                .map(row -> new PendingOutboxEvent(
                        row.getId(),
                        row.getEventType(),
                        row.getStreamKey(),
                        row.getPartitionKey(),
                        row.getPayload(),
                        row.getRetryCount()
                ))
                .toList();
    }

    @Override
    public void markSent(UUID eventId) {
        requireSingleRow(mapper.markSent(eventId), eventId, "标记已投递");
    }

    @Override
    public void scheduleRetry(UUID eventId, int retryCount, Duration delay, String lastError) {
        requireSingleRow(
                mapper.scheduleRetry(eventId, retryCount, delay.toMillis(), lastError),
                eventId,
                "安排重试"
        );
    }

    @Override
    public void park(UUID eventId, int retryCount, String lastError) {
        requireSingleRow(mapper.park(eventId, retryCount, lastError), eventId, "转入 parked");
    }

    private static void requireSingleRow(int affectedRows, UUID eventId, String action) {
        if (affectedRows != 1) {
            throw new IllegalStateException(
                    "Outbox " + action + "未命中唯一待投递行: eventId=" + eventId + ", affectedRows=" + affectedRows
            );
        }
    }
}
