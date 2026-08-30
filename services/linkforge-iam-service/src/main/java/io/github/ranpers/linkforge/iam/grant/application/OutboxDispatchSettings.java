package io.github.ranpers.linkforge.iam.grant.application;

import java.time.Duration;

/** Outbox 投递批次与失败策略;maxAttempts 表示包含首次发送在内的总尝试上限。 */
public record OutboxDispatchSettings(
        int batchSize,
        int maxAttempts,
        Duration baseBackoff,
        Duration maxBackoff,
        Duration sendTimeout
) {

    public OutboxDispatchSettings {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize 必须大于 0");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts 必须大于 0");
        }
        requirePositive(baseBackoff, "baseBackoff");
        requirePositive(maxBackoff, "maxBackoff");
        requirePositive(sendTimeout, "sendTimeout");
        if (maxBackoff.compareTo(baseBackoff) < 0) {
            throw new IllegalArgumentException("maxBackoff 不能小于 baseBackoff");
        }
    }

    public Duration retryDelay(int failedAttempt) {
        if (failedAttempt < 1) {
            throw new IllegalArgumentException("failedAttempt 必须大于 0");
        }
        Duration delay = baseBackoff;
        for (int attempt = 1; attempt < failedAttempt && delay.compareTo(maxBackoff) < 0; attempt++) {
            if (delay.compareTo(maxBackoff.dividedBy(2)) > 0) {
                return maxBackoff;
            }
            delay = delay.multipliedBy(2);
        }
        return delay.compareTo(maxBackoff) > 0 ? maxBackoff : delay;
    }

    private static void requirePositive(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " 必须为正数");
        }
    }
}
