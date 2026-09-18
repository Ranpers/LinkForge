package io.github.ranpers.linkforge.link.resolution.application;

import io.github.ranpers.linkforge.link.resolution.application.port.out.ShortDomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkResolutionQuery;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkResolutionSnapshot;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFacts;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
public class CachedLinkResolutionQuery implements LinkResolutionQuery {

    private final LinkRuntimeFactSource source;
    private final LinkRuntimeCache cache;

    public CachedLinkResolutionQuery(LinkRuntimeFactSource source, LinkRuntimeCache cache) {
        this.source = source;
        this.cache = cache;
    }

    @Override
    public Optional<LinkResolutionSnapshot> find(String host, String linkCode) {
        String normalizedHost = host.trim().toLowerCase(Locale.ROOT);
        LinkRuntimeFacts link = cache.findLink(normalizedHost, linkCode)
                .or(() -> loadLink(normalizedHost, linkCode))
                .orElse(null);
        if (link == null) {
            return Optional.empty();
        }
        ShortDomainRuntimeState shortDomain = cache.findShortDomain(link.shortDomainId())
                .or(() -> loadShortDomain(link))
                .orElse(null);
        if (shortDomain == null) {
            return Optional.empty();
        }
        UserSecurityRestrictionSet restrictions =
                cache.findRestrictions(link.createdByUserId())
                        .orElseGet(() -> loadRestrictions(link));
        boolean restricted = restrictions.restrictions().stream()
                .anyMatch(rule -> rule.matches(link.createdAt()));
        return Optional.of(new LinkResolutionSnapshot(
                link.targetUrl(),
                link.status(),
                link.deletedAt(),
                link.expiresAt(),
                shortDomain.enabled(),
                restricted
        ));
    }

    private Optional<LinkRuntimeFacts> loadLink(String host, String linkCode) {
        Optional<LinkRuntimeFacts> loaded = source.findLink(host, linkCode);
        loaded.ifPresent(cache::putLink);
        return loaded;
    }

    private Optional<ShortDomainRuntimeState> loadShortDomain(LinkRuntimeFacts link) {
        Optional<ShortDomainRuntimeState> loaded = source.findShortDomain(link.shortDomainId());
        loaded.ifPresent(cache::putShortDomain);
        return loaded;
    }

    private UserSecurityRestrictionSet loadRestrictions(LinkRuntimeFacts link) {
        UserSecurityRestrictionSet loaded =
                source.findRestrictions(link.createdByUserId());
        cache.putRestrictions(loaded);
        return loaded;
    }
}
