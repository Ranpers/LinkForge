package io.github.ranpers.linkforge.iam.domain.application;

import io.github.ranpers.linkforge.iam.domain.application.port.in.CreateDomainCommand;
import io.github.ranpers.linkforge.iam.domain.application.port.in.CreateDomainUseCase;
import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.in.UpdateDomainCommand;
import io.github.ranpers.linkforge.iam.domain.application.port.in.UpdateDomainUseCase;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainStore;
import io.github.ranpers.linkforge.iam.domain.domain.DomainHost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DomainCommandService implements CreateDomainUseCase, UpdateDomainUseCase {

    private static final String CREATE_PERMISSION = "domain:create";
    private static final String UPDATE_PERMISSION = "domain:update";

    private final DomainStore store;

    public DomainCommandService(DomainStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public DomainListItem create(CreateDomainCommand command) {
        DomainHost host = new DomainHost(command.host());
        DomainStore.CreationOutcome outcome = store.create(
                command.actorUserId(),
                host.value(),
                command.name(),
                command.traceId()
        );
        return switch (outcome.result()) {
            case CREATED -> outcome.created();
            case HOST_CONFLICT -> throw new DomainHostAlreadyExistsException();
            case DENIED -> throw new DomainWriteDeniedException(CREATE_PERMISSION);
        };
    }

    @Override
    @Transactional
    public void update(UpdateDomainCommand command) {
        switch (store.update(command.actorUserId(), command.domainId(), command.name())) {
            case UPDATED, UNCHANGED -> {
            }
            case NOT_FOUND -> throw new DomainNotFoundException();
            case DENIED -> throw new DomainWriteDeniedException(UPDATE_PERMISSION);
        }
    }
}
