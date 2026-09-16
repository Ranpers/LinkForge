package io.github.ranpers.linkforge.iam.domain.application.port.out;

import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;

import java.util.List;

/** 域名列表只读持久化端口。 */
public interface DomainListQuery {
    List<DomainListItem> find(DomainListCriteria criteria);
}
