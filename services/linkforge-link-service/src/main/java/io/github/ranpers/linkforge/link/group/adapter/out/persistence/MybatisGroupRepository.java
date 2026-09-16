package io.github.ranpers.linkforge.link.group.adapter.out.persistence;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisGroupRepository implements GroupRepository {

    private static final int NAME_CONFLICT_CODE = 0;
    private static final int UPDATED_CODE = 1;

    private final GroupMapper mapper;

    public MybatisGroupRepository(GroupMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UUID insert(UUID ownerUserId, String name, int sortOrder) {
        return mapper.insert(ownerUserId, name, sortOrder);
    }

    @Override
    public UpdateResult update(UUID ownerUserId, UUID groupId, String name, Integer sortOrder) {
        final Integer result;
        try {
            result = mapper.update(ownerUserId, groupId, name, sortOrder);
        } catch (DuplicateKeyException exception) {
            return UpdateResult.NAME_CONFLICT;
        }
        if (result == null) {
            return UpdateResult.NOT_FOUND;
        }
        return switch (result) {
            case UPDATED_CODE -> UpdateResult.UPDATED;
            case NAME_CONFLICT_CODE -> UpdateResult.NAME_CONFLICT;
            default -> UpdateResult.NOT_FOUND;
        };
    }

    @Override
    public boolean softDelete(UUID ownerUserId, UUID groupId) {
        if (mapper.lockActiveOwned(ownerUserId, groupId) == null) {
            return false;
        }
        return mapper.softDelete(ownerUserId, groupId) == 1;
    }

    @Override
    public GroupView find(UUID ownerUserId, UUID groupId) {
        return mapper.find(ownerUserId, groupId);
    }
}
