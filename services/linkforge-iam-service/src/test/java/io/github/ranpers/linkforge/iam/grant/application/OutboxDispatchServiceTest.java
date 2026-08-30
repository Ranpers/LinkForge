package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationEventPublisher;
import io.github.ranpers.linkforge.iam.grant.application.port.out.OutboxDispatchStore;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxDispatchServiceTest {

    private final OutboxDispatchStore store = mock(OutboxDispatchStore.class);
    private final AuthorizationEventPublisher publisher = mock(AuthorizationEventPublisher.class);
    private final OutboxDispatchService service = new OutboxDispatchService(store, publisher);
    private final OutboxDispatchSettings settings = new OutboxDispatchSettings(
            10,
            3,
            Duration.ofSeconds(1),
            Duration.ofSeconds(4),
            Duration.ofSeconds(2)
    );

    @Test
    void shouldMarkAcknowledgedEventAsSent() {
        PendingOutboxEvent event = event(0);
        when(store.lockDueRows(10)).thenReturn(List.of(event));

        OutboxDispatchSummary summary = service.dispatchBatch(settings);

        verify(publisher).publish(event, Duration.ofSeconds(2));
        verify(store).markSent(event.id());
        assertEquals(new OutboxDispatchSummary(1, 1, 0, 0, List.of()), summary);
    }

    @Test
    void shouldScheduleExponentialRetryAfterPublishFailure() {
        PendingOutboxEvent event = event(1);
        when(store.lockDueRows(10)).thenReturn(List.of(event));
        doThrow(new IllegalStateException("broker unavailable"))
                .when(publisher).publish(event, Duration.ofSeconds(2));

        OutboxDispatchSummary summary = service.dispatchBatch(settings);

        verify(store).scheduleRetry(
                event.id(),
                2,
                Duration.ofSeconds(2),
                "IllegalStateException: broker unavailable"
        );
        verify(store, never()).markSent(event.id());
        assertEquals(new OutboxDispatchSummary(1, 0, 1, 0, List.of()), summary);
    }

    @Test
    void shouldStopBatchAfterFirstPublishFailure() {
        PendingOutboxEvent failed = event(0);
        PendingOutboxEvent untouched = event(0);
        when(store.lockDueRows(10)).thenReturn(List.of(failed, untouched));
        doThrow(new IllegalStateException("broker unavailable"))
                .when(publisher).publish(failed, Duration.ofSeconds(2));

        OutboxDispatchSummary summary = service.dispatchBatch(settings);

        verify(store).scheduleRetry(
                failed.id(),
                1,
                Duration.ofSeconds(1),
                "IllegalStateException: broker unavailable"
        );
        verify(publisher, never()).publish(untouched, Duration.ofSeconds(2));
        verify(store, never()).markSent(untouched.id());
        assertEquals(new OutboxDispatchSummary(2, 0, 1, 0, List.of()), summary);
    }

    @Test
    void shouldParkAfterMaximumAttempts() {
        PendingOutboxEvent event = event(2);
        when(store.lockDueRows(10)).thenReturn(List.of(event));
        doThrow(new IllegalStateException("permanent failure"))
                .when(publisher).publish(event, Duration.ofSeconds(2));

        OutboxDispatchSummary summary = service.dispatchBatch(settings);

        verify(store).park(event.id(), 3, "IllegalStateException: permanent failure");
        verify(store, never()).scheduleRetry(
                event.id(), 3, Duration.ofSeconds(4), "IllegalStateException: permanent failure"
        );
        assertEquals(new OutboxDispatchSummary(1, 0, 0, 1, List.of(event.id())), summary);
    }

    @Test
    void shouldFailWholeBatchWhenDatabaseStateTransitionFails() {
        PendingOutboxEvent event = event(0);
        when(store.lockDueRows(10)).thenReturn(List.of(event));
        doThrow(new IllegalStateException("row disappeared")).when(store).markSent(event.id());

        assertThrows(IllegalStateException.class, () -> service.dispatchBatch(settings));
        verify(store, never()).scheduleRetry(
                event.id(), 1, Duration.ofSeconds(1), "IllegalStateException: row disappeared"
        );
    }

    private static PendingOutboxEvent event(int retryCount) {
        UUID id = UUID.randomUUID();
        return new PendingOutboxEvent(
                id,
                "UserDomainGrantChanged",
                "USER_DOMAIN:user:domain",
                "domain",
                "{\"eventId\":\"" + id + "\"}",
                retryCount
        );
    }
}
