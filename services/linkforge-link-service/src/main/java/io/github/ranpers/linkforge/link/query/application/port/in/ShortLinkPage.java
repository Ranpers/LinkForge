package io.github.ranpers.linkforge.link.query.application.port.in;

import java.util.List;

/** 短链接游标页。 */
public record ShortLinkPage(List<ShortLinkListItem> items, boolean hasMore) {
    public ShortLinkPage {
        items = List.copyOf(items);
    }
}
