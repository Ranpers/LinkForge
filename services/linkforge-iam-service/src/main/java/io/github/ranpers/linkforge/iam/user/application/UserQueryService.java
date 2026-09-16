package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.user.application.port.in.ListUsersQuery;
import io.github.ranpers.linkforge.iam.user.application.port.in.ListUsersUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserListItem;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserPage;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserListCriteria;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserListQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserQueryService implements ListUsersUseCase {

    private final UserListQuery userListQuery;

    public UserQueryService(UserListQuery userListQuery) {
        this.userListQuery = userListQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPage list(ListUsersQuery query) {
        Integer status = query.status() == null ? null : query.status().databaseValue();
        List<UserListItem> rows = userListQuery.find(new UserListCriteria(
                status,
                query.searchTerm(),
                query.cursorCreatedAt(),
                query.cursorId(),
                query.pageSize() + 1
        ));
        boolean hasMore = rows.size() > query.pageSize();
        List<UserListItem> items = hasMore ? rows.subList(0, query.pageSize()) : rows;
        return new UserPage(items, hasMore);
    }
}
