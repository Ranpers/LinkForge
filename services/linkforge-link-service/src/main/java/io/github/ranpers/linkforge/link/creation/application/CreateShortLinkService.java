package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkUseCase;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.application.port.out.IamAuthorizationGateway;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkIdGenerator;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkRepository;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
public class CreateShortLinkService implements CreateShortLinkUseCase {

    private final IamAuthorizationGateway authorizationGateway;
    private final ShortLinkRepository repository;
    private final ShortLinkIdGenerator idGenerator;

    public CreateShortLinkService(
            IamAuthorizationGateway authorizationGateway,
            ShortLinkRepository repository,
            ShortLinkIdGenerator idGenerator
    ) {
        this.authorizationGateway = authorizationGateway;
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public CreatedShortLink create(CreateShortLinkCommand command) {
        Objects.requireNonNull(command, "command");
        String fingerprint = RequestFingerprint.of(command);
        var existing = repository.findByIdempotencyKey(
                command.actorUserId(), command.idempotencyKey()
        );
        if (existing.isPresent()) {
            return resolveReplay(existing.get(), fingerprint);
        }
        if (command.groupId() != null
                && !repository.groupBelongsToUser(command.groupId(), command.actorUserId())) {
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

        ShortLink link = new ShortLink(
                idGenerator.nextId(),
                command.actorUserId(),
                command.groupId(),
                command.name(),
                command.linkCode(),
                command.fullUrl(),
                command.sortOrder(),
                command.domainId(),
                command.expiresAt(),
                command.idempotencyKey(),
                fingerprint,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (repository.insertIfIdempotencyAbsent(link)) {
            return CreatedShortLink.from(link);
        }
        return resolveReplay(
                repository.findByIdempotencyKey(
                                command.actorUserId(), command.idempotencyKey())
                        .orElseThrow(() -> new IllegalStateException(
                                "幂等约束冲突后未找到原创建结果"
                        )),
                fingerprint
        );
    }

    private static CreatedShortLink resolveReplay(ShortLink existing, String fingerprint) {
        if (!existing.requestFingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException();
        }
        return CreatedShortLink.from(existing);
    }
}
