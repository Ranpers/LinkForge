package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkUseCase;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.application.port.out.IamAuthorizationGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class CreateShortLinkService implements CreateShortLinkUseCase {

    private final IamAuthorizationGateway authorizationGateway;
    private final CreateShortLinkTransaction transaction;

    public CreateShortLinkService(
            IamAuthorizationGateway authorizationGateway,
            CreateShortLinkTransaction transaction
    ) {
        this.authorizationGateway = authorizationGateway;
        this.transaction = transaction;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CreatedShortLink create(CreateShortLinkCommand command) {
        Objects.requireNonNull(command, "command");
        String fingerprint = RequestFingerprint.of(command);
        var replay = transaction.findReplay(command, fingerprint);
        if (replay.isPresent()) {
            return replay.get();
        }
        if (command.groupId() != null
                && !transaction.groupBelongsToUser(command.groupId(), command.actorUserId())) {
            throw new InvalidLinkGroupException();
        }

        LinkCreationAuthorization authorization = authorizationGateway.validate(
                command.actorUserId(), command.domainId()
        );
        if (!authorization.allowed()) {
            throw new LinkCreationDeniedException(
                    authorization.reasonCode(), authorization.decisionId()
            );
        }

        return transaction.createAuthorized(command, fingerprint);
    }
}
