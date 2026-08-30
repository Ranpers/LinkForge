package io.github.ranpers.linkforge.iam.grant.application;

import java.util.List;
import java.util.UUID;

public record OutboxDispatchSummary(
        int locked,
        int sent,
        int retried,
        int parked,
        List<UUID> parkedEventIds
) {

    public static OutboxDispatchSummary empty() {
        return new OutboxDispatchSummary(0, 0, 0, 0, List.of());
    }
}
