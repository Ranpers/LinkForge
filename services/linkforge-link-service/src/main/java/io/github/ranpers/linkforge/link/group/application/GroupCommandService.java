package io.github.ranpers.linkforge.link.group.application;

import io.github.ranpers.linkforge.link.group.application.port.in.CreateGroupCommand;
import io.github.ranpers.linkforge.link.group.application.port.in.CreateGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.DeleteGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.in.UpdateGroupCommand;
import io.github.ranpers.linkforge.link.group.application.port.in.UpdateGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupRepository;
import io.github.ranpers.linkforge.link.group.domain.GroupName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GroupCommandService implements CreateGroupUseCase, UpdateGroupUseCase, DeleteGroupUseCase {

    private final GroupRepository groupRepository;

    public GroupCommandService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    @Transactional
    public GroupView create(CreateGroupCommand command) {
        GroupName name = new GroupName(command.name());
        UUID groupId = groupRepository.insert(
                command.ownerUserId(),
                name.value(),
                command.sortOrder()
        );
        if (groupId == null) {
            throw new GroupAlreadyExistsException();
        }
        return groupRepository.find(command.ownerUserId(), groupId);
    }

    @Override
    @Transactional
    public void update(UpdateGroupCommand command) {
        GroupName name = new GroupName(command.name());
        GroupRepository.UpdateResult result = groupRepository.update(
                command.ownerUserId(),
                command.groupId(),
                name.value(),
                command.sortOrder()
        );
        switch (result) {
            case UPDATED -> {
            }
            case NAME_CONFLICT -> throw new GroupAlreadyExistsException();
            case NOT_FOUND -> throw new GroupNotFoundException();
        }
    }

    @Override
    @Transactional
    public void delete(UUID ownerUserId, UUID groupId) {
        if (!groupRepository.softDelete(ownerUserId, groupId)) {
            throw new GroupNotFoundException();
        }
    }
}
