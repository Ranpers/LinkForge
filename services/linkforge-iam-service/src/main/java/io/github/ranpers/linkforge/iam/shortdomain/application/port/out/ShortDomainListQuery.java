package io.github.ranpers.linkforge.iam.shortdomain.application.port.out;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;

import java.util.List;

/** 域名列表只读持久化端口。 */
public interface ShortDomainListQuery {
    List<ShortDomainListItem> find(ShortDomainListCriteria criteria);
}
