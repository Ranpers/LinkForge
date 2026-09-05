package io.github.ranpers.linkforge.link.control.application;

import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCheckpoint;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCache;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlInbox;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlProjection;
import io.github.ranpers.linkforge.link.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.link.control.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LinkControlEventHandlerTest {

    private final LinkControlInbox inbox = mock(LinkControlInbox.class);
    private final LinkControlCheckpoint checkpoint = mock(LinkControlCheckpoint.class);
    private final LinkControlProjection projection = mock(LinkControlProjection.class);
    private final LinkControlCache cache = mock(LinkControlCache.class);
    private final LinkControlEventHandler handler =
            new LinkControlEventHandler(inbox, checkpoint, projection, cache);

    @Test
    void duplicateEventDoesNotTouchCheckpointOrProjection() {
        LinkControlEvent event = event(2);
        when(inbox.recordIfNew(event)).thenReturn(false);

        LinkControlEventHandlingResult result = handler.handle(event);

        assertEquals(LinkControlEventHandlingResult.duplicate(), result);
        verify(checkpoint, never()).ensureExists(event.streamKey());
        verify(projection, never()).apply(event);
        verify(cache, never()).projectAfterCommit(event);
    }

    @Test
    void staleRevisionIsRecordedButDoesNotOverwriteProjection() {
        LinkControlEvent event = event(2);
        when(inbox.recordIfNew(event)).thenReturn(true);
        when(checkpoint.lockAndGetRevision(event.streamKey())).thenReturn(3L);

        LinkControlEventHandlingResult result = handler.handle(event);

        assertEquals(LinkControlEventHandlingResult.stale(), result);
        verify(checkpoint).ensureExists(event.streamKey());
        verify(projection, never()).apply(event);
        verify(checkpoint, never()).advance(event.streamKey(), event.revision());
        verify(cache, never()).projectAfterCommit(event);
    }

    @Test
    void newerRevisionUpdatesProjectionBeforeCheckpoint() {
        LinkControlEvent event = event(4);
        when(inbox.recordIfNew(event)).thenReturn(true);
        when(checkpoint.lockAndGetRevision(event.streamKey())).thenReturn(3L);
        when(projection.apply(event)).thenReturn(1);

        LinkControlEventHandlingResult result = handler.handle(event);

        assertEquals(LinkControlEventHandlingResult.applied(1), result);
        verify(projection).apply(event);
        verify(checkpoint).advance(event.streamKey(), event.revision());
        verify(cache).projectAfterCommit(event);
    }

    private static LinkControlEvent event(long revision) {
        UUID domainId = UUID.randomUUID();
        return new DomainAvailabilityChanged(
                UUID.randomUUID(),
                1,
                "DOMAIN:" + domainId,
                revision,
                OffsetDateTime.parse("2026-09-04T00:00:00Z"),
                new ControlEventTraceId("trace"),
                domainId,
                "go.example.com",
                false
        );
    }
}
