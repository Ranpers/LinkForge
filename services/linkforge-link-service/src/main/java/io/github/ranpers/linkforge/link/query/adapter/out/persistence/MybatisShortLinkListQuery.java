package io.github.ranpers.linkforge.link.query.adapter.out.persistence;

import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListItem;
import io.github.ranpers.linkforge.link.query.application.port.out.ShortLinkListCriteria;
import io.github.ranpers.linkforge.link.query.application.port.out.ShortLinkListQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisShortLinkListQuery implements ShortLinkListQuery {

    private final ShortLinkListMapper mapper;

    public MybatisShortLinkListQuery(ShortLinkListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ShortLinkListItem> find(ShortLinkListCriteria criteria) {
        return mapper.find(criteria);
    }
}
