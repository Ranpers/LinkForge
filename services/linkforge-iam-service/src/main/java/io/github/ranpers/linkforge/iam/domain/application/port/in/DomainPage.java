package io.github.ranpers.linkforge.iam.domain.application.port.in;

import java.util.List;

/** 域名游标页。 */
public record DomainPage(List<DomainListItem> items, boolean hasMore) {
    public DomainPage {
        items = List.copyOf(items);
    }
}
