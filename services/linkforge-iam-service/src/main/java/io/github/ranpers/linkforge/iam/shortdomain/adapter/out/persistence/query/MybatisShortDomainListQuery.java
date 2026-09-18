package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainListCriteria;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainListQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisShortDomainListQuery implements ShortDomainListQuery {

    private final ShortDomainListMapper mapper;

    public MybatisShortDomainListQuery(ShortDomainListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ShortDomainListItem> find(ShortDomainListCriteria criteria) {
        return mapper.find(criteria);
    }
}
