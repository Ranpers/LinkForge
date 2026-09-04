package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface LinkRuntimeCache {
    Optional<LinkRuntimeFacts> findLink(String host, String linkCode);

    Optional<DomainRuntimeState> findDomain(UUID domainId);

    Optional<UserSecurityRestrictionSet> findRestrictions(UUID userId);

    void putLink(LinkRuntimeFacts link);

    void putDomain(DomainRuntimeState domain);

    void putRestrictions(UserSecurityRestrictionSet restrictions);
}
