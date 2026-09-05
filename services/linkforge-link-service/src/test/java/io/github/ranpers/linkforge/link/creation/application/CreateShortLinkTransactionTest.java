package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.application.port.in.ShortCodeRequest;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortCodeGenerator;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkIdGenerator;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkRepository;
import io.github.ranpers.linkforge.link.creation.domain.ShortCode;
import io.github.ranpers.linkforge.link.creation.domain.ShortCodeType;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateShortLinkTransactionTest {

    private final ShortLinkRepository repository = mock(ShortLinkRepository.class);
    private final ShortLinkIdGenerator idGenerator = mock(ShortLinkIdGenerator.class);
    private final ShortCodeGenerator codeGenerator = mock(ShortCodeGenerator.class);
    private final CreateShortLinkTransaction transaction =
            new CreateShortLinkTransaction(repository, idGenerator, codeGenerator);
    private final UUID userId = UUID.randomUUID();
    private final UUID domainId = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();

    @Test
    void retriesGeneratedCodeCollisionWithoutChangingLinkIdentity() {
        CreateShortLinkCommand command = command(ShortCodeRequest.auto());
        ShortCode first = ShortCode.generated("AAAAAAAAAA");
        ShortCode second = ShortCode.generated("BBBBBBBBBB");
        when(idGenerator.nextId()).thenReturn(linkId);
        when(codeGenerator.nextCode()).thenReturn(first.value(), second.value());
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(repository.tryInsert(any())).thenReturn(false, true);
        when(repository.existsByDomainAndCode(domainId, first)).thenReturn(true);

        CreatedShortLink result =
                transaction.createAuthorized(command, RequestFingerprint.of(command));

        assertEquals(
                new CreatedShortLink(linkId, second.value(), domainId, ShortCodeType.GENERATED),
                result
        );
        ArgumentCaptor<ShortLink> links = ArgumentCaptor.forClass(ShortLink.class);
        verify(repository, times(2)).tryInsert(links.capture());
        assertEquals(linkId, links.getAllValues().get(0).id());
        assertEquals(linkId, links.getAllValues().get(1).id());
        assertEquals(first, links.getAllValues().get(0).shortCode());
        assertEquals(second, links.getAllValues().get(1).shortCode());
    }

    @Test
    void returnsConcurrentIdempotencyReplayBeforeClassifyingCodeConflict() {
        CreateShortLinkCommand command = command(ShortCodeRequest.auto());
        String fingerprint = RequestFingerprint.of(command);
        ShortCode candidate = ShortCode.generated("AAAAAAAAAA");
        ShortLink existing = existingGeneratedLink(fingerprint);
        when(idGenerator.nextId()).thenReturn(linkId);
        when(codeGenerator.nextCode()).thenReturn(candidate.value());
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(repository.tryInsert(any())).thenReturn(false);

        CreatedShortLink result = transaction.createAuthorized(command, fingerprint);

        assertEquals(CreatedShortLink.from(existing), result);
        verify(repository, never()).existsByDomainAndCode(domainId, candidate);
    }

    @Test
    void customCodeConflictDoesNotRetry() {
        ShortCode customCode = ShortCode.custom("my-code");
        CreateShortLinkCommand command =
                command(new ShortCodeRequest.Custom(customCode));
        when(idGenerator.nextId()).thenReturn(linkId);
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(repository.tryInsert(any())).thenReturn(false);
        when(repository.existsByDomainAndCode(domainId, customCode)).thenReturn(true);

        assertThrows(
                ShortCodeAlreadyExistsException.class,
                () -> transaction.createAuthorized(command, RequestFingerprint.of(command))
        );

        verify(codeGenerator, never()).nextCode();
        verify(repository).tryInsert(any());
    }

    @Test
    void failsAllocationAfterFiveGeneratedCollisions() {
        CreateShortLinkCommand command = command(ShortCodeRequest.auto());
        ShortCode candidate = ShortCode.generated("AAAAAAAAAA");
        when(idGenerator.nextId()).thenReturn(linkId);
        when(codeGenerator.nextCode()).thenReturn(candidate.value());
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(repository.tryInsert(any())).thenReturn(false);
        when(repository.existsByDomainAndCode(domainId, candidate)).thenReturn(true);

        assertThrows(
                ShortCodeAllocationException.class,
                () -> transaction.createAuthorized(command, RequestFingerprint.of(command))
        );

        verify(codeGenerator, times(5)).nextCode();
        verify(repository, times(5)).tryInsert(any());
    }

    @Test
    void rejectsUnclassifiedIgnoredInsert() {
        CreateShortLinkCommand command = command(ShortCodeRequest.auto());
        ShortCode candidate = ShortCode.generated("AAAAAAAAAA");
        when(idGenerator.nextId()).thenReturn(linkId);
        when(codeGenerator.nextCode()).thenReturn(candidate.value());
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(repository.tryInsert(any())).thenReturn(false);

        assertThrows(
                ShortLinkPersistenceInvariantException.class,
                () -> transaction.createAuthorized(command, RequestFingerprint.of(command))
        );
    }

    @Test
    void treatsInternalIdCollisionAsPersistenceInvariantFailure() {
        CreateShortLinkCommand command = command(ShortCodeRequest.auto());
        ShortCode candidate = ShortCode.generated("AAAAAAAAAA");
        when(idGenerator.nextId()).thenReturn(linkId);
        when(codeGenerator.nextCode()).thenReturn(candidate.value());
        when(repository.findByIdempotencyKey(userId, command.idempotencyKey()))
                .thenReturn(Optional.empty());
        when(repository.tryInsert(any())).thenReturn(false);
        when(repository.existsById(linkId)).thenReturn(true);

        assertThrows(
                ShortLinkPersistenceInvariantException.class,
                () -> transaction.createAuthorized(command, RequestFingerprint.of(command))
        );

        verify(repository, never()).existsByDomainAndCode(domainId, candidate);
    }

    private CreateShortLinkCommand command(ShortCodeRequest shortCodeRequest) {
        return new CreateShortLinkCommand(
                userId,
                null,
                "Example",
                shortCodeRequest,
                "https://example.com",
                0,
                domainId,
                null,
                "request-1"
        );
    }

    private ShortLink existingGeneratedLink(String fingerprint) {
        return new ShortLink(
                UUID.randomUUID(),
                userId,
                null,
                "Example",
                ShortCode.generated("CCCCCCCCCC"),
                "https://example.com",
                0,
                domainId,
                null,
                "request-1",
                fingerprint,
                OffsetDateTime.parse("2026-09-05T00:00:00Z")
        );
    }
}
