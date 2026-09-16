package io.github.ranpers.linkforge.link.group.application.port.in;

import java.util.List;

/** 分组游标页。 */
public record GroupPage(List<GroupView> items, boolean hasMore) {
    public GroupPage {
        items = List.copyOf(items);
    }
}
