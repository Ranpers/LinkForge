package io.github.ranpers.linkforge.link.resolution.application;

import io.github.ranpers.linkforge.link.resolution.application.port.out.DomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFacts;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestriction;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedLinkResolutionQueryTest {

    private final LinkRuntimeFactSource source = mock(LinkRuntimeFactSource.class);
    private final LinkRuntimeCache cache = mock(LinkRuntimeCache.class);
    private final CachedLinkResolutionQuery query =
            new CachedLinkResolutionQuery(source, cache);
    private final UUID domainId = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final OffsetDateTime createdAt =
            OffsetDateTime.parse("2026-09-04T00:00:00Z");
    private final LinkRuntimeFacts link = new LinkRuntimeFacts(
            UUID.randomUUID(),
            domainId,
            creatorId,
            "go.example.com",
            "code",
            "https://example.com",
            "ACTIVE",
            null,
            null,
            createdAt,
            2
    );

    @Test
    void composesCachedFactsInsteadOfCachingFinalAvailability() {
        when(cache.findLink("go.example.com", "code")).thenReturn(Optional.of(link));
        when(cache.findDomain(domainId))
                .thenReturn(Optional.of(new DomainRuntimeState(domainId, false, 4)));
        when(cache.findRestrictions(creatorId))
                .thenReturn(Optional.of(new UserSecurityRestrictionSet(
                        creatorId,
                        3,
                        List.of()
                )));

        var result = query.find("GO.EXAMPLE.COM", "code").orElseThrow();

        assertFalse(result.domainEnabled());
        assertFalse(result.securityRestricted());
        verify(source, never()).findLink("go.example.com", "code");
    }

    @Test
    void cacheMissLoadsAndPopulatesEachIndependentFact() {
        DomainRuntimeState domain = new DomainRuntimeState(domainId, true, 2);
        UserSecurityRestrictionSet restrictions =
                new UserSecurityRestrictionSet(creatorId, 0, List.of());
        when(cache.findLink("go.example.com", "code")).thenReturn(Optional.empty());
        when(source.findLink("go.example.com", "code")).thenReturn(Optional.of(link));
        when(cache.findDomain(domainId)).thenReturn(Optional.empty());
        when(source.findDomain(domainId)).thenReturn(Optional.of(domain));
        when(cache.findRestrictions(creatorId)).thenReturn(Optional.empty());
        when(source.findRestrictions(creatorId)).thenReturn(restrictions);

        assertTrue(query.find("go.example.com", "code").isPresent());

        verify(cache).putLink(link);
        verify(cache).putDomain(domain);
        verify(cache).putRestrictions(restrictions);
    }

    @Test
    void evaluatesCreatedDuringAgainstImmutableLinkCreationTime() {
        UserSecurityRestriction rule = new UserSecurityRestriction(
                UUID.randomUUID(),
                "CREATED_DURING",
                createdAt.minusMinutes(1),
                createdAt.plusMinutes(1),
                "ACCOUNT_COMPROMISED",
                createdAt.plusDays(1)
        );
        when(cache.findLink("go.example.com", "code")).thenReturn(Optional.of(link));
        when(cache.findDomain(domainId))
                .thenReturn(Optional.of(new DomainRuntimeState(domainId, true, 1)));
        when(cache.findRestrictions(creatorId))
                .thenReturn(Optional.of(new UserSecurityRestrictionSet(
                        creatorId,
                        2,
                        List.of(rule)
                )));

        assertTrue(query.find("go.example.com", "code").orElseThrow().securityRestricted());
    }
}
