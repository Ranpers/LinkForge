package io.github.ranpers.linkforge.iam.security.application;

import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;
import io.github.ranpers.linkforge.iam.security.application.port.out.LinkSecurityRestrictionStore;
import io.github.ranpers.linkforge.iam.security.domain.LinkSecurityRestriction;
import io.github.ranpers.linkforge.iam.security.domain.RestrictionMode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinkSecurityRestrictionServiceTest {

    private final LinkSecurityRestrictionStore store = mock(LinkSecurityRestrictionStore.class);
    private final LinkSecurityRestrictionService service = new LinkSecurityRestrictionService(store);
    private final UUID actorUserId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();

    @Test
    void returnsCreatedRestrictionId() {
        CreateLinkSecurityRestrictionCommand command = command();
        UUID restrictionId = UUID.randomUUID();
        when(store.create(command)).thenReturn(new LinkSecurityRestrictionStore.CreateResult(
                LinkSecurityRestrictionStore.MutationOutcome.CHANGED,
                restrictionId
        ));

        assertEquals(restrictionId, service.create(command));
    }

    @Test
    void deniesUnauthorizedActor() {
        CreateLinkSecurityRestrictionCommand command = command();
        when(store.create(command)).thenReturn(new LinkSecurityRestrictionStore.CreateResult(
                LinkSecurityRestrictionStore.MutationOutcome.DENIED,
                null
        ));

        assertThrows(SecurityDispositionDeniedException.class, () -> service.create(command));
    }

    @Test
    void revokeIsIdempotentButMissingRestrictionIsNot() {
        UUID restrictionId = UUID.randomUUID();
        when(store.revoke(actorUserId, targetUserId, restrictionId, "trace"))
                .thenReturn(LinkSecurityRestrictionStore.MutationOutcome.UNCHANGED);

        service.revoke(actorUserId, targetUserId, restrictionId, "trace");

        UUID missingId = UUID.randomUUID();
        when(store.revoke(actorUserId, targetUserId, missingId, "trace"))
                .thenReturn(LinkSecurityRestrictionStore.MutationOutcome.RESTRICTION_NOT_FOUND);
        assertThrows(
                LinkSecurityRestrictionNotFoundException.class,
                () -> service.revoke(actorUserId, targetUserId, missingId, "trace")
        );
    }

    private CreateLinkSecurityRestrictionCommand command() {
        return new CreateLinkSecurityRestrictionCommand(
                actorUserId,
                targetUserId,
                new LinkSecurityRestriction(
                        RestrictionMode.ALL,
                        null,
                        null,
                        "ACCOUNT_COMPROMISED"
                ),
                "trace"
        );
    }
}
