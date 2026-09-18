package io.github.ranpers.linkforge.iam.shortdomain.application;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainPage;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ListShortDomainsQuery;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ListShortDomainsUseCase;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainListCriteria;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainListQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShortDomainQueryService implements ListShortDomainsUseCase {

    private final ShortDomainListQuery domainListQuery;

    public ShortDomainQueryService(ShortDomainListQuery domainListQuery) {
        this.domainListQuery = domainListQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public ShortDomainPage list(ListShortDomainsQuery query) {
        List<ShortDomainListItem> rows = domainListQuery.find(new ShortDomainListCriteria(
                query.actorUserId(),
                query.canReadAll(),
                query.enabled(),
                query.searchTerm(),
                query.cursorCreatedAt(),
                query.cursorId(),
                query.pageSize() + 1
        ));
        boolean hasMore = rows.size() > query.pageSize();
        List<ShortDomainListItem> items = hasMore ? rows.subList(0, query.pageSize()) : rows;
        return new ShortDomainPage(items, hasMore);
    }
}
