package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import io.github.ranpers.linkforge.link.resolution.application.port.out.ShortDomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFacts;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestriction;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisLinkResolutionQuery implements LinkRuntimeFactSource {

    private final LinkResolutionMapper mapper;

    public MybatisLinkResolutionQuery(LinkResolutionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<LinkRuntimeFacts> findLink(String host, String linkCode) {
        return Optional.ofNullable(mapper.findLinkByRoute(host, linkCode))
                .map(MybatisLinkResolutionQuery::toFacts);
    }

    @Override
    public Optional<LinkRuntimeFacts> findLink(java.util.UUID linkId) {
        return Optional.ofNullable(mapper.findLinkById(linkId))
                .map(MybatisLinkResolutionQuery::toFacts);
    }

    @Override
    public Optional<ShortDomainRuntimeState> findShortDomain(java.util.UUID shortDomainId) {
        ShortDomainRuntimeStateRow row = mapper.findShortDomain(shortDomainId);
        return row == null
                ? Optional.empty()
                : Optional.of(new ShortDomainRuntimeState(
                        row.shortDomainId(), row.enabled(), row.revision()
                ));
    }

    @Override
    public UserSecurityRestrictionSet findRestrictions(java.util.UUID userId) {
        Long revision = mapper.findRestrictionRevision(
                "USER_LINK_SECURITY:" + userId
        );
        var restrictions = mapper.findRestrictions(userId).stream()
                .map(row -> new UserSecurityRestriction(
                        row.restrictionId(),
                        row.mode(),
                        row.rangeStart(),
                        row.rangeEnd(),
                        row.reasonCode(),
                        row.createdAt()
                ))
                .toList();
        return new UserSecurityRestrictionSet(
                userId,
                revision == null ? 0 : revision,
                restrictions
        );
    }

    private static LinkRuntimeFacts toFacts(LinkResolutionRow row) {
        return new LinkRuntimeFacts(
                row.linkId(),
                row.shortDomainId(),
                row.createdByUserId(),
                row.host(),
                row.linkCode(),
                row.targetUrl(),
                row.linkStatus(),
                row.deletedAt(),
                row.expiresAt(),
                row.createdAt(),
                row.revision()
        );
    }
}
