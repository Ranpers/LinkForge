package io.github.ranpers.linkforge.iam.grant.application;

import java.util.UUID;

/** 待投递事件的最小应用层视图;payload 已是契约规定的完整扁平 JSON。 */
public record PendingOutboxEvent(
        UUID id,
        String eventType,
        String streamKey,
        String partitionKey,
        String payload,
        int retryCount
) {
}
