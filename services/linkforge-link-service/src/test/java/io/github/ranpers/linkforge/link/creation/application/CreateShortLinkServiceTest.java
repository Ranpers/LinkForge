package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.application.port.out.IamAuthorizationGateway;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkIdGenerator;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkRepository;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateShortLinkServiceTest {

    private final IamAuthorizationGateway authorization = mock(IamAuthorizationGateway.class);
    private final ShortLinkRepository repository = mock(ShortLinkRepository.class);
    private final ShortLinkIdGenerator idGenerator = mock(ShortLinkIdGenerator.class);
    private final CreateShortLinkTransaction transaction =
            new CreateShortLinkTransaction(repository, idGenerator);
    private final CreateShortLinkService service =
            new CreateShortLinkService(authorization, transaction);
    private final UUID userId = UUID.randomUUID();
    private final UUID domainId = UUID.randomUUID();
    private final CreateShortLinkCommand command = new CreateShortLinkCommand(
            userId,
            null,
            "Example",
            "code",
            "https://example.com",
            0,
            domainId,
            null,
            "request-1"
    );

    @Test
    void returnsExistingReplayWithoutCallingIam() {
        String fingerprint = RequestFingerprint.of(command);
        ShortLink existing = new ShortLink(
                UUID.randomUUID(),
                userId,
                null,
                command.name(),
                command.linkCode(),
                command.fullUrl(),
                command.sortOrder(),
                domainId,
                null,
                command.idempotencyKey(),
                fingerprint,
                OffsetDateTime.parse("2026-09-05T00:00:00Z")
        );
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.of(existing));

        CreatedShortLink result = service.create(command);

        assertEquals(CreatedShortLink.from(existing), result);
        verify(authorization, never()).validate(any(), any());
        verify(repository, never()).insertIfIdempotencyAbsent(any());
    }

    @Test
    void authorizesOutsideWriteTransactionAndThenCreates() {
        UUID linkId = UUID.randomUUID();
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(authorization.validate(userId, domainId)).thenReturn(new LinkCreationAuthorization(
                true,
                "ALLOWED",
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-09-05T00:00:00Z")
        ));
        when(idGenerator.nextId()).thenReturn(linkId);
        when(repository.insertIfIdempotencyAbsent(any())).thenReturn(true);

        CreatedShortLink result = service.create(command);

        assertEquals(new CreatedShortLink(linkId, command.linkCode(), domainId), result);
        verify(authorization).validate(userId, domainId);
        verify(repository).insertIfIdempotencyAbsent(any());
    }

    @Test
    void suspendsAnyCallerTransactionAroundRemoteAuthorization() throws NoSuchMethodException {
        Transactional boundary = CreateShortLinkService.class
                .getMethod("create", CreateShortLinkCommand.class)
                .getAnnotation(Transactional.class);

        assertEquals(Propagation.NOT_SUPPORTED, boundary.propagation());
    }
}
