package io.github.ranpers.linkforge.iam.security.application;

import io.github.ranpers.linkforge.iam.security.application.port.out.UserSecurityStatusStore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityStatusServiceTest {

    private final UserSecurityStatusStore store = mock(UserSecurityStatusStore.class);
    private final UserSecurityStatusService service = new UserSecurityStatusService(store);
    private final UUID actor = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void acceptsChangedAndIdempotentStatus() {
        when(store.change(actor, target, true, "trace"))
                .thenReturn(UserSecurityStatusStore.ChangeOutcome.CHANGED);
        when(store.change(actor, target, false, "trace"))
                .thenReturn(UserSecurityStatusStore.ChangeOutcome.UNCHANGED);

        assertDoesNotThrow(() -> service.change(actor, target, true, "trace"));
        assertDoesNotThrow(() -> service.change(actor, target, false, "trace"));
    }

    @Test
    void cannotUseSecurityUnfreezeToReactivateDeactivatedUser() {
        when(store.change(actor, target, false, "trace"))
                .thenReturn(UserSecurityStatusStore.ChangeOutcome.LIFECYCLE_CONFLICT);

        assertThrows(
                UserSecurityStatusConflictException.class,
                () -> service.change(actor, target, false, "trace")
        );
    }
}
