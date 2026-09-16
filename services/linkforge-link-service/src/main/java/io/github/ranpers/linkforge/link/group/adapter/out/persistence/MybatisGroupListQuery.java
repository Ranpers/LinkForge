package io.github.ranpers.linkforge.link.group.adapter.out.persistence;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupListCriteria;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupListQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisGroupListQuery implements GroupListQuery {

    private final GroupListMapper mapper;

    public MybatisGroupListQuery(GroupListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GroupView> find(GroupListCriteria criteria) {
        return mapper.find(criteria);
    }
}
