package io.github.ranpers.linkforge.link.creation.adapter.out.persistence;

import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkRepository;
import io.github.ranpers.linkforge.link.creation.domain.ShortCode;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MybatisShortLinkRepository implements ShortLinkRepository {

    private final ShortLinkMapper mapper;

    public MybatisShortLinkRepository(ShortLinkMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ShortLink> findByIdempotencyKey(UUID userId, String idempotencyKey) {
        return Optional.ofNullable(mapper.findByIdempotencyKey(userId, idempotencyKey))
                .map(MybatisShortLinkRepository::toDomain);
    }

    @Override
    public boolean groupBelongsToUser(UUID groupId, UUID userId) {
        return mapper.groupBelongsToUser(groupId, userId);
    }

    @Override
    public boolean tryInsert(ShortLink link) {
        return mapper.tryInsert(link) == 1;
    }

    @Override
    public boolean existsById(UUID linkId) {
        return mapper.existsById(linkId);
    }

    @Override
    public boolean existsByDomainAndCode(UUID domainId, ShortCode shortCode) {
        return mapper.existsByDomainAndCode(domainId, shortCode.value());
    }

    private static ShortLink toDomain(ShortLinkRow row) {
        return new ShortLink(
                row.id(),
                row.createdByUserId(),
                row.groupId(),
                row.name(),
                new ShortCode(row.linkCode(), row.codeType()),
                row.fullUrl(),
                row.sortOrder(),
                row.domainId(),
                row.expiresAt(),
                row.idempotencyKey(),
                row.requestFingerprint(),
                row.createdAt()
        );
    }
}
