package io.github.ranpers.linkforge.iam.grant.application;

import java.time.Duration;

/**
 * 约束 Outbox 投递批次、发送超时和指数退避策略。
 *
 * @param batchSize   单次锁定的最大事件数，必须大于零
 * @param maxAttempts 包含首次发送在内的总尝试上限，必须大于零
 * @param baseBackoff 首次失败后的正数退避时长
 * @param maxBackoff  退避上限，不得小于基础退避
 * @param sendTimeout 单条消息等待代理确认的正数超时
 */
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

    /**
     * 计算指定连续失败次数后的指数退避时长。
     *
     * @param failedAttempt 已发生的失败次数，从 {@code 1} 开始
     * @return 不超过 {@link #maxBackoff()} 的退避时长
     */
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
