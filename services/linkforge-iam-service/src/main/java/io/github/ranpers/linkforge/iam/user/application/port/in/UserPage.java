package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.List;

/** 用户游标页。 */
public record UserPage(List<UserListItem> items, boolean hasMore) {
    public UserPage {
        items = List.copyOf(items);
    }
}
