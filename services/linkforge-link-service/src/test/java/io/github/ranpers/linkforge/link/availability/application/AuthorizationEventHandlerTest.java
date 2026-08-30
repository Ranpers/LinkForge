package io.github.ranpers.linkforge.link.availability.application;

import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationInbox;
import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationStreamCheckpoint;
import io.github.ranpers.linkforge.link.availability.application.port.out.LinkAvailabilityProjection;
import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;
import io.github.ranpers.linkforge.link.availability.domain.UserDomainGrantChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationEventHandlerTest {

    @Mock
    private AuthorizationInbox inbox;

    @Mock
    private AuthorizationStreamCheckpoint checkpoint;

    @Mock
    private LinkAvailabilityProjection projection;

    @Test
    void shouldStopImmediatelyForDuplicateEventId() {
        AuthorizationEvent event = event(3);
        when(inbox.recordIfNew(event)).thenReturn(false);

        var result = handler().handle(event);

        assertEquals(AuthorizationEventHandlingResult.duplicate(), result);
        verify(checkpoint, never()).ensureExists(event.streamKey());
        verify(projection, never()).applyTargetState(event);
    }

    @Test
    void shouldKeepInboxButSkipStaleRevision() {
        AuthorizationEvent event = event(3);
        when(inbox.recordIfNew(event)).thenReturn(true);
        when(checkpoint.lockAndGetRevision(event.streamKey())).thenReturn(4L);

        var result = handler().handle(event);

        assertEquals(AuthorizationEventHandlingResult.stale(), result);
        verify(projection, never()).applyTargetState(event);
        verify(checkpoint, never()).advance(event.streamKey(), event.revision());
    }

    @Test
    void shouldApplyAndAdvanceInRequiredOrder() {
        AuthorizationEvent event = event(3);
        when(inbox.recordIfNew(event)).thenReturn(true);
        when(checkpoint.lockAndGetRevision(event.streamKey())).thenReturn(2L);
        when(projection.applyTargetState(event)).thenReturn(17);

        var result = handler().handle(event);

        assertEquals(AuthorizationEventHandlingResult.applied(17), result);
        InOrder order = inOrder(inbox, checkpoint, projection);
        order.verify(inbox).recordIfNew(event);
        order.verify(checkpoint).ensureExists(event.streamKey());
        order.verify(checkpoint).lockAndGetRevision(event.streamKey());
        order.verify(projection).applyTargetState(event);
        order.verify(checkpoint).advance(event.streamKey(), event.revision());
    }

    private AuthorizationEventHandler handler() {
        return new AuthorizationEventHandler(inbox, checkpoint, projection);
    }

    private static AuthorizationEvent event(long revision) {
        UUID userId = UUID.fromString("0198f7c4-3ee6-7000-8000-000000000001");
        UUID domainId = UUID.fromString("0198f7c4-3ee6-7000-8000-000000000002");
        return new UserDomainGrantChanged(
                UUID.randomUUID(),
                "USER_DOMAIN:" + userId + ":" + domainId,
                revision,
                OffsetDateTime.parse("2026-08-30T10:00:00+08:00"),
                userId,
                domainId,
                false
        );
    }
}
