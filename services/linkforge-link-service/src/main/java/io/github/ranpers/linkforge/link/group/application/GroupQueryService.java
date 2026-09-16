package io.github.ranpers.linkforge.link.group.application;

import io.github.ranpers.linkforge.link.group.application.port.in.GetGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.GroupPage;
import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.in.ListGroupsQuery;
import io.github.ranpers.linkforge.link.group.application.port.in.ListGroupsUseCase;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupListCriteria;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupListQuery;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupQueryService implements GetGroupUseCase, ListGroupsUseCase {

    private final GroupRepository groupRepository;
    private final GroupListQuery groupListQuery;

    public GroupQueryService(GroupRepository groupRepository, GroupListQuery groupListQuery) {
        this.groupRepository = groupRepository;
        this.groupListQuery = groupListQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public GroupView get(UUID ownerUserId, UUID groupId) {
        GroupView group = groupRepository.find(ownerUserId, groupId);
        if (group == null) {
            throw new GroupNotFoundException();
        }
        return group;
    }

    @Override
    @Transactional(readOnly = true)
    public GroupPage list(ListGroupsQuery query) {
        List<GroupView> rows = groupListQuery.find(new GroupListCriteria(
                query.ownerUserId(),
                query.cursorCreatedAt(),
                query.cursorId(),
                query.pageSize() + 1
        ));
        boolean hasMore = rows.size() > query.pageSize();
        List<GroupView> items = hasMore ? rows.subList(0, query.pageSize()) : rows;
        return new GroupPage(items, hasMore);
    }
}
