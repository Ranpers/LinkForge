package io.github.ranpers.linkforge.link.management.application;

import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementAuthorizationGateway;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementCache;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementRepository;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementSnapshot;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManageShortLinkServiceTest {

    private final LinkManagementRepository repository = mock(LinkManagementRepository.class);
    private final LinkManagementAuthorizationGateway authorization =
            mock(LinkManagementAuthorizationGateway.class);
    private final LinkManagementCache cache = mock(LinkManagementCache.class);
    private final ManageShortLinkService service =
            new ManageShortLinkService(repository, authorization, cache);
    private final UUID actor = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();
    private final UUID domainId = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final LinkManagementSnapshot link =
            new LinkManagementSnapshot(linkId, domainId, creatorId);

    @Test
    void usesPersistedCreatorAndDomainForAuthorization() {
        when(repository.find(linkId)).thenReturn(link);
        when(authorization.validate(actor, domainId, creatorId, LinkManagementAction.UPDATE))
                .thenReturn(allowed());
        when(repository.updateTarget(linkId, "https://example.com/new")).thenReturn(true);

        service.updateTarget(actor, linkId, "https://example.com/new");

        verify(authorization).validate(
                actor,
                domainId,
                creatorId,
                LinkManagementAction.UPDATE
        );
        verify(repository).updateTarget(linkId, "https://example.com/new");
        verify(cache).refreshAfterCommit(linkId);
    }

    @Test
    void deniedDecisionCannotMutateLink() {
        when(repository.find(linkId)).thenReturn(link);
        when(authorization.validate(actor, domainId, creatorId, LinkManagementAction.DELETE))
                .thenReturn(new LinkManagementAuthorization(
                        false,
                        "LINK_MANAGEMENT_NOT_ALLOWED",
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-09-04T00:00:00Z")
                ));

        assertThrows(LinkManagementDeniedException.class, () -> service.delete(actor, linkId));

        verify(repository, never()).softDelete(linkId);
        verify(cache, never()).refreshAfterCommit(linkId);
    }

    @Test
    void cannotRestoreNonManualDisabledLink() {
        when(repository.find(linkId)).thenReturn(link);
        when(authorization.validate(actor, domainId, creatorId, LinkManagementAction.UPDATE))
                .thenReturn(allowed());
        when(repository.changeAvailability(linkId, true))
                .thenReturn(LinkManagementRepository.AvailabilityChangeResult.FORBIDDEN_STATE);

        assertThrows(
                LinkStateConflictException.class,
                () -> service.changeAvailability(actor, linkId, true)
        );
    }

    private static LinkManagementAuthorization allowed() {
        return new LinkManagementAuthorization(
                true,
                "ALLOWED",
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-09-04T00:00:00Z")
        );
    }
}
