package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface LinkRuntimeFactSource {
    Optional<LinkRuntimeFacts> findLink(String host, String linkCode);

    Optional<LinkRuntimeFacts> findLink(UUID linkId);

    Optional<DomainRuntimeState> findDomain(UUID domainId);

    UserSecurityRestrictionSet findRestrictions(UUID userId);
}
