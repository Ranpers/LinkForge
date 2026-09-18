package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisShortDomainStore implements ShortDomainStore {

    private static final int HOST_CONFLICT_CODE = 1;
    private static final int CREATED_CODE = 2;
    private static final int DENIED_CODE = 0;

    private static final int NOT_FOUND_CODE = 1;
    private static final int UPDATED_CODE = 2;
    private static final int UNCHANGED_CODE = 3;

    private final ShortDomainStoreMapper mapper;

    public MybatisShortDomainStore(ShortDomainStoreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CreationOutcome create(
            UUID actorUserId,
            String host,
            String name,
            ControlEventRequestId requestId
    ) {
        ShortDomainCreationRow row = mapper.create(
                actorUserId,
                host,
                name,
                requestId == null ? null : requestId.value()
        );
        Integer code = row == null ? null : row.resultCode();
        return switch (code == null ? DENIED_CODE : code) {
            case CREATED_CODE -> new CreationOutcome(CreationResult.CREATED, row.toItem());
            case HOST_CONFLICT_CODE -> new CreationOutcome(CreationResult.HOST_CONFLICT, null);
            default -> new CreationOutcome(CreationResult.DENIED, null);
        };
    }

    @Override
    public UpdateResult update(UUID actorUserId, UUID shortDomainId, String name) {
        Integer result = mapper.update(actorUserId, shortDomainId, name);
        return switch (result == null ? DENIED_CODE : result) {
            case UPDATED_CODE -> UpdateResult.UPDATED;
            case UNCHANGED_CODE -> UpdateResult.UNCHANGED;
            case NOT_FOUND_CODE -> UpdateResult.NOT_FOUND;
            default -> UpdateResult.DENIED;
        };
    }
}
