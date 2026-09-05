package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.security.application.port.out.UserSecurityStatusStore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MybatisUserSecurityStatusStoreTest {

    private final UserSecurityStatusMapper statusMapper = mock(UserSecurityStatusMapper.class);
    private final LinkSecurityRestrictionMapper restrictionMapper =
            mock(LinkSecurityRestrictionMapper.class);
    private final MybatisUserSecurityStatusStore store =
            new MybatisUserSecurityStatusStore(statusMapper, restrictionMapper);
    private final UUID actorUserId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();
    private final ControlEventTraceId traceId = new ControlEventTraceId("trace");

    @Test
    void freezeCreatesSystemRestrictionAndPublishesSnapshot() {
        when(statusMapper.change(actorUserId, targetUserId, true)).thenReturn(4);
        when(statusMapper.activateAccountSuspensionRestriction(targetUserId)).thenReturn(1);
        when(restrictionMapper.incrementRevision(targetUserId)).thenReturn(7L);
        when(restrictionMapper.appendSnapshotEvent(targetUserId, 7L, "trace")).thenReturn(1);

        UserSecurityStatusStore.ChangeOutcome outcome =
                store.change(actorUserId, targetUserId, true, traceId);

        assertEquals(UserSecurityStatusStore.ChangeOutcome.CHANGED, outcome);
        verify(restrictionMapper).appendSnapshotEvent(targetUserId, 7L, "trace");
    }

    @Test
    void unfreezeRevokesOnlySystemRestrictionAndPublishesSnapshot() {
        when(statusMapper.change(actorUserId, targetUserId, false)).thenReturn(4);
        when(statusMapper.revokeAccountSuspensionRestriction(targetUserId)).thenReturn(1);
        when(restrictionMapper.incrementRevision(targetUserId)).thenReturn(8L);
        when(restrictionMapper.appendSnapshotEvent(targetUserId, 8L, "trace")).thenReturn(1);

        UserSecurityStatusStore.ChangeOutcome outcome =
                store.change(actorUserId, targetUserId, false, traceId);

        assertEquals(UserSecurityStatusStore.ChangeOutcome.CHANGED, outcome);
        verify(statusMapper).revokeAccountSuspensionRestriction(targetUserId);
        verify(restrictionMapper).appendSnapshotEvent(targetUserId, 8L, "trace");
    }

    @Test
    void repeatedRequestWithoutRestrictionChangeDoesNotPublishSnapshot() {
        when(statusMapper.change(actorUserId, targetUserId, true)).thenReturn(3);
        when(statusMapper.activateAccountSuspensionRestriction(targetUserId)).thenReturn(0);

        UserSecurityStatusStore.ChangeOutcome outcome =
                store.change(actorUserId, targetUserId, true, traceId);

        assertEquals(UserSecurityStatusStore.ChangeOutcome.UNCHANGED, outcome);
        verifyNoInteractions(restrictionMapper);
    }

    @Test
    void repeatedFreezeRepairsMissingSystemRestriction() {
        when(statusMapper.change(actorUserId, targetUserId, true)).thenReturn(3);
        when(statusMapper.activateAccountSuspensionRestriction(targetUserId)).thenReturn(1);
        when(restrictionMapper.incrementRevision(targetUserId)).thenReturn(9L);
        when(restrictionMapper.appendSnapshotEvent(targetUserId, 9L, "trace")).thenReturn(1);

        UserSecurityStatusStore.ChangeOutcome outcome =
                store.change(actorUserId, targetUserId, true, traceId);

        assertEquals(UserSecurityStatusStore.ChangeOutcome.CHANGED, outcome);
        verify(restrictionMapper).appendSnapshotEvent(targetUserId, 9L, "trace");
    }

    @Test
    void deniedChangeDoesNotTouchRestrictions() {
        when(statusMapper.change(actorUserId, targetUserId, true)).thenReturn(0);

        UserSecurityStatusStore.ChangeOutcome outcome =
                store.change(actorUserId, targetUserId, true, traceId);

        assertEquals(UserSecurityStatusStore.ChangeOutcome.DENIED, outcome);
        verify(statusMapper, never()).activateAccountSuspensionRestriction(targetUserId);
        verifyNoInteractions(restrictionMapper);
    }

    @Test
    void failedSnapshotAppendAbortsTheAtomicChange() {
        when(statusMapper.change(actorUserId, targetUserId, true)).thenReturn(4);
        when(statusMapper.activateAccountSuspensionRestriction(targetUserId)).thenReturn(1);
        when(restrictionMapper.incrementRevision(targetUserId)).thenReturn(9L);
        when(restrictionMapper.appendSnapshotEvent(targetUserId, 9L, "trace")).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> store.change(actorUserId, targetUserId, true, traceId)
        );
    }
}
