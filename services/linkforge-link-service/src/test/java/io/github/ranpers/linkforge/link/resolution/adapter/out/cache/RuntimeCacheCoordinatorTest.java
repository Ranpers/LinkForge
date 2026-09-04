package io.github.ranpers.linkforge.link.resolution.adapter.out.cache;

import io.github.ranpers.linkforge.link.control.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkSecurityRestriction;
import io.github.ranpers.linkforge.link.control.domain.RestrictionMode;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import io.github.ranpers.linkforge.link.resolution.application.port.out.DomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RuntimeCacheCoordinatorTest {

    private final LinkRuntimeFactSource source = mock(LinkRuntimeFactSource.class);
    private final LinkRuntimeCache cache = mock(LinkRuntimeCache.class);
    private final RuntimeCacheCoordinator coordinator =
            new RuntimeCacheCoordinator(source, cache);

    @Test
    void projectsDomainRevisionIntoSharedCache() {
        UUID domainId = UUID.randomUUID();
        coordinator.projectAfterCommit(new DomainAvailabilityChanged(
                UUID.randomUUID(),
                1,
                "DOMAIN:" + domainId,
                7,
                OffsetDateTime.parse("2026-09-04T00:00:00Z"),
                "trace",
                domainId,
                "go.example.com",
                false
        ));

        verify(cache).putDomain(new DomainRuntimeState(domainId, false, 7));
    }

    @Test
    void projectsCompleteUserRestrictionSnapshotIntoSharedCache() {
        UUID userId = UUID.randomUUID();
        UUID restrictionId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-04T00:00:00Z");
        coordinator.projectAfterCommit(new UserLinkSecurityRestrictionsChanged(
                UUID.randomUUID(),
                1,
                "USER_LINK_SECURITY:" + userId,
                3,
                createdAt,
                "trace",
                userId,
                List.of(new LinkSecurityRestriction(
                        restrictionId,
                        RestrictionMode.ALL,
                        null,
                        null,
                        "ACCOUNT_COMPROMISED",
                        createdAt
                ))
        ));

        verify(cache).putRestrictions(argThat(snapshot ->
                snapshot.userId().equals(userId)
                        && snapshot.revision() == 3
                        && snapshot.restrictions().size() == 1
                        && snapshot.restrictions().getFirst().restrictionId()
                        .equals(restrictionId)
        ));
    }
}
