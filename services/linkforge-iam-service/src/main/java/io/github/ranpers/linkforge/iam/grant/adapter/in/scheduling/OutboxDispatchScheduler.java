package io.github.ranpers.linkforge.iam.grant.adapter.in.scheduling;

import io.github.ranpers.linkforge.iam.grant.application.OutboxDispatchService;
import io.github.ranpers.linkforge.iam.grant.application.OutboxDispatchSettings;
import io.github.ranpers.linkforge.iam.grant.application.OutboxDispatchSummary;
import io.github.ranpers.linkforge.iam.grant.config.OutboxDispatchProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "linkforge.outbox", name = "enabled", havingValue = "true")
public class OutboxDispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatchScheduler.class);

    private final OutboxDispatchService dispatchService;
    private final OutboxDispatchSettings settings;
    private final Counter sentCounter;
    private final Counter retriedCounter;
    private final Counter parkedCounter;
    private final Counter batchFailureCounter;

    public OutboxDispatchScheduler(
            OutboxDispatchService dispatchService,
            OutboxDispatchProperties properties,
            MeterRegistry meterRegistry
    ) {
        this.dispatchService = dispatchService;
        this.settings = properties.settings();
        this.sentCounter = counter(meterRegistry, "sent");
        this.retriedCounter = counter(meterRegistry, "retried");
        this.parkedCounter = counter(meterRegistry, "parked");
        this.batchFailureCounter = Counter.builder("linkforge.iam.outbox.batch.failures")
                .description("Outbox 批次因数据库或基础设施异常整体回滚的次数")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${linkforge.outbox.poll-interval:1s}")
    public void dispatch() {
        try {
            OutboxDispatchSummary summary = dispatchService.dispatchBatch(settings);
            sentCounter.increment(summary.sent());
            retriedCounter.increment(summary.retried());
            parkedCounter.increment(summary.parked());
            if (summary.parked() > 0) {
                log.error("Outbox 批次产生 parked 事件: eventIds={}", summary.parkedEventIds());
            }
        } catch (RuntimeException exception) {
            batchFailureCounter.increment();
            log.error("Outbox 投递批次整体失败,事务已回滚,将在下一轮重试", exception);
        }
    }

    private static Counter counter(MeterRegistry registry, String outcome) {
        return Counter.builder("linkforge.iam.outbox.events")
                .tag("outcome", outcome)
                .description("IAM Outbox 投递状态流转数量")
                .register(registry);
    }
}
