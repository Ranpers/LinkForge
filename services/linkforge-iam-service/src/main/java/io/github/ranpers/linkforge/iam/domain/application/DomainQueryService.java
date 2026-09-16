package io.github.ranpers.linkforge.iam.domain.application;

import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainPage;
import io.github.ranpers.linkforge.iam.domain.application.port.in.ListDomainsQuery;
import io.github.ranpers.linkforge.iam.domain.application.port.in.ListDomainsUseCase;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainListCriteria;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainListQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DomainQueryService implements ListDomainsUseCase {

    private final DomainListQuery domainListQuery;

    public DomainQueryService(DomainListQuery domainListQuery) {
        this.domainListQuery = domainListQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public DomainPage list(ListDomainsQuery query) {
        List<DomainListItem> rows = domainListQuery.find(new DomainListCriteria(
                query.actorUserId(),
                query.canReadAll(),
                query.enabled(),
                query.searchTerm(),
                query.cursorCreatedAt(),
                query.cursorId(),
                query.pageSize() + 1
        ));
        boolean hasMore = rows.size() > query.pageSize();
        List<DomainListItem> items = hasMore ? rows.subList(0, query.pageSize()) : rows;
        return new DomainPage(items, hasMore);
    }
}
