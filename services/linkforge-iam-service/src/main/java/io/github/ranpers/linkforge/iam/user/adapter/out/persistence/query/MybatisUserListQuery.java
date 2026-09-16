package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.user.application.port.in.UserListItem;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserListCriteria;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserListQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisUserListQuery implements UserListQuery {

    private final UserListMapper mapper;

    public MybatisUserListQuery(UserListMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<UserListItem> find(UserListCriteria criteria) {
        return mapper.find(criteria);
    }
}
