package io.github.ranpers.linkforge.iam.shortdomain.application;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.CreateShortDomainCommand;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.CreateShortDomainUseCase;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.UpdateShortDomainCommand;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.UpdateShortDomainUseCase;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainStore;
import io.github.ranpers.linkforge.iam.shortdomain.domain.ShortDomainHost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortDomainCommandService implements CreateShortDomainUseCase, UpdateShortDomainUseCase {

    private static final String CREATE_PERMISSION = "short-domain:create";
    private static final String UPDATE_PERMISSION = "short-domain:update";

    private final ShortDomainStore store;

    public ShortDomainCommandService(ShortDomainStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public ShortDomainListItem create(CreateShortDomainCommand command) {
        ShortDomainHost host = new ShortDomainHost(command.host());
        ShortDomainStore.CreationOutcome outcome = store.create(
                command.actorUserId(),
                host.value(),
                command.name(),
                command.requestId()
        );
        return switch (outcome.result()) {
            case CREATED -> outcome.created();
            case HOST_CONFLICT -> throw new ShortDomainHostAlreadyExistsException();
            case DENIED -> throw new ShortDomainWriteDeniedException(CREATE_PERMISSION);
        };
    }

    @Override
    @Transactional
    public void update(UpdateShortDomainCommand command) {
        switch (store.update(command.actorUserId(), command.shortDomainId(), command.name())) {
            case UPDATED, UNCHANGED -> {
            }
            case NOT_FOUND -> throw new ShortDomainNotFoundException();
            case DENIED -> throw new ShortDomainWriteDeniedException(UPDATE_PERMISSION);
        }
    }
}
