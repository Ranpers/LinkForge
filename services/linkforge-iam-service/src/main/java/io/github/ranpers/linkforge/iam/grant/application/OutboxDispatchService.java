package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkControlEventPublisher;
import io.github.ranpers.linkforge.iam.grant.application.port.out.OutboxDispatchStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 事务性 Outbox 投递编排。
 *
 * <p>数据库锁会跨 Kafka ACK 等待持有。Kafka ACK 后若进程在数据库提交前崩溃,
 * 事件会再次发送,这是契约要求的 at-least-once 窗口,由 Link Inbox 按 eventId 去重。</p>
 */
@Service
public class OutboxDispatchService {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatchService.class);
    private static final int MAX_ERROR_LENGTH = 2000;

    private final OutboxDispatchStore store;
    private final LinkControlEventPublisher publisher;

    public OutboxDispatchService(OutboxDispatchStore store, LinkControlEventPublisher publisher) {
        this.store = store;
        this.publisher = publisher;
    }

    /**
     * 在单个数据库事务内投递一批到期事件并推进其状态。
     *
     * <p>遇到首个代理故障即停止本批次，避免按批次数量累加发送超时。代理确认后、
     * 数据库提交前崩溃可能造成重复投递，消费者必须按事件 ID 去重。</p>
     *
     * @param settings 批次大小、最大尝试次数、退避和发送超时设置
     * @return 本批次锁定、发送、重试和 parked 的数量摘要
     */
    @Transactional
    public OutboxDispatchSummary dispatchBatch(OutboxDispatchSettings settings) {
        List<PendingOutboxEvent> events = store.lockDueRows(settings.batchSize());
        if (events.isEmpty()) {
            return OutboxDispatchSummary.empty();
        }

        int sent = 0;
        int retried = 0;
        List<UUID> parkedIds = new ArrayList<>();

        for (PendingOutboxEvent event : events) {
            try {
                publisher.publish(event, settings.sendTimeout());
            } catch (RuntimeException exception) {
                if (handlePublishFailure(event, settings, exception, parkedIds)) {
                    retried++;
                }
                // Kafka 故障通常是系统性的;失败后结束本批次,避免 batchSize * sendTimeout 的长事务。
                break;
            }
            store.markSent(event.id());
            sent++;
        }

        return new OutboxDispatchSummary(
                events.size(),
                sent,
                retried,
                parkedIds.size(),
                List.copyOf(parkedIds)
        );
    }

    /** @return true=已安排重试,false=已 parked。 */
    private boolean handlePublishFailure(
            PendingOutboxEvent event,
            OutboxDispatchSettings settings,
            RuntimeException exception,
            List<UUID> parkedIds
    ) {
        int failedAttempt = event.retryCount() + 1;
        String error = errorSummary(exception);
        if (failedAttempt >= settings.maxAttempts()) {
            store.park(event.id(), failedAttempt, error);
            parkedIds.add(event.id());
            log.error(
                    "Outbox 事件进入 parked: eventId={}, streamKey={}, failedAttempts={}",
                    event.id(), event.streamKey(), failedAttempt, exception
            );
            return false;
        }

        Duration delay = settings.retryDelay(failedAttempt);
        store.scheduleRetry(event.id(), failedAttempt, delay, error);
        log.warn(
                "Outbox 投递失败,已安排重试: eventId={}, streamKey={}, failedAttempts={}, delay={}",
                event.id(), event.streamKey(), failedAttempt, delay, exception
        );
        return true;
    }

    private static String errorSummary(RuntimeException exception) {
        Throwable root = exception;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getClass().getSimpleName()
                + (root.getMessage() == null || root.getMessage().isBlank() ? "" : ": " + root.getMessage());
        return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
    }
}
