package io.github.ranpers.linkforge.iam.shortdomain.application.port.in;

import java.util.List;

/** 域名游标页。 */
public record ShortDomainPage(List<ShortDomainListItem> items, boolean hasMore) {
    public ShortDomainPage {
        items = List.copyOf(items);
    }
}
