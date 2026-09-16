package io.github.ranpers.linkforge.link.resolution.application;

import io.github.ranpers.linkforge.link.resolution.application.port.in.ResolveShortLinkUseCase;
import io.github.ranpers.linkforge.link.resolution.application.port.in.ResolvedShortLink;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkResolutionQuery;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkResolutionSnapshot;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class ResolveShortLinkService implements ResolveShortLinkUseCase {

    private final LinkResolutionQuery query;

    public ResolveShortLinkService(LinkResolutionQuery query) {
        this.query = query;
    }

    @Override
    public ResolvedShortLink resolve(String host, String linkCode) {
        LinkResolutionSnapshot link = query.find(host, linkCode)
                .orElseThrow(ShortLinkUnavailableException::new);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!"ACTIVE".equals(link.linkStatus())
                || link.deletedAt() != null
                || link.expiresAt() != null && !link.expiresAt().isAfter(now)
                || !link.domainEnabled()
                || link.securityRestricted()) {
            throw new ShortLinkUnavailableException();
        }
        return new ResolvedShortLink(link.targetUrl());
    }
}
