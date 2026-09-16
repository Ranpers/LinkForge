package io.github.ranpers.linkforge.link.query.application.port.out;

import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListItem;

import java.util.List;

/** 短链接列表只读持久化端口。 */
public interface ShortLinkListQuery {
    List<ShortLinkListItem> find(ShortLinkListCriteria criteria);
}
