package io.github.ranpers.linkforge.link.query.application;

import io.github.ranpers.linkforge.link.query.application.port.in.ListShortLinksQuery;
import io.github.ranpers.linkforge.link.query.application.port.in.ListShortLinksUseCase;
import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListItem;
import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkPage;
import io.github.ranpers.linkforge.link.query.application.port.out.ShortLinkListCriteria;
import io.github.ranpers.linkforge.link.query.application.port.out.ShortLinkListQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShortLinkQueryService implements ListShortLinksUseCase {

    private final ShortLinkListQuery shortLinkListQuery;

    public ShortLinkQueryService(ShortLinkListQuery shortLinkListQuery) {
        this.shortLinkListQuery = shortLinkListQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public ShortLinkPage list(ListShortLinksQuery query) {
        UUID effectiveCreator = query.canReadAll()
                ? query.createdByUserId()
                : query.actorUserId();
        List<ShortLinkListItem> rows = shortLinkListQuery.find(new ShortLinkListCriteria(
                effectiveCreator,
                query.domainId(),
                query.status(),
                query.searchTerm(),
                query.cursorCreatedAt(),
                query.cursorId(),
                query.pageSize() + 1
        ));
        boolean hasMore = rows.size() > query.pageSize();
        List<ShortLinkListItem> items = hasMore
                ? rows.subList(0, query.pageSize())
                : rows;
        return new ShortLinkPage(items, hasMore);
    }
}
