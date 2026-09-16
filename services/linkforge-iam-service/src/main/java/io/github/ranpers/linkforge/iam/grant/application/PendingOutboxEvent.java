package io.github.ranpers.linkforge.iam.grant.application;

import java.util.UUID;

/**
 * 表示已经序列化且等待代理确认的 Outbox 事件。
 *
 * @param id           用于消费者幂等去重的事件标识
 * @param eventType    契约定义的事件类型
 * @param streamKey    用于修订顺序检查的逻辑流标识
 * @param partitionKey Kafka 分区键
 * @param payload      契约规定的完整 JSON 信封
 * @param retryCount   已记录的失败次数
 */
public record PendingOutboxEvent(
        UUID id,
        String eventType,
        String streamKey,
        String partitionKey,
        String payload,
        int retryCount
) {
}
