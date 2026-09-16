package io.github.ranpers.linkforge.iam.domain.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainListCriteria;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainListQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisDomainListQuery implements DomainListQuery {

    private final DomainListMapper mapper;

    public MybatisDomainListQuery(DomainListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<DomainListItem> find(DomainListCriteria criteria) {
        return mapper.find(criteria);
    }
}
