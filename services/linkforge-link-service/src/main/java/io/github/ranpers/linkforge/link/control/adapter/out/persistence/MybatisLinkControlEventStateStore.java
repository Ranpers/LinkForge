package io.github.ranpers.linkforge.link.control.adapter.out.persistence;

import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCheckpoint;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlInbox;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlProjection;
import io.github.ranpers.linkforge.link.control.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisLinkControlEventStateStore implements
        LinkControlInbox,
        LinkControlCheckpoint,
        LinkControlProjection {

    private final LinkControlEventMapper mapper;

    public MybatisLinkControlEventStateStore(LinkControlEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean recordIfNew(LinkControlEvent event) {
        return mapper.insertInbox(
                event.eventId(),
                event.eventType().wireName(),
                event.schemaVersion(),
                event.streamKey(),
                event.traceId() == null ? null : event.traceId().value()
        ) == 1;
    }

    @Override
    public void ensureExists(String streamKey) {
        mapper.insertCheckpointIfAbsent(streamKey);
    }

    @Override
    public long lockAndGetRevision(String streamKey) {
        Long revision = mapper.lockCheckpoint(streamKey);
        if (revision == null) {
            throw new IllegalStateException("Link 控制检查点不存在: " + streamKey);
        }
        return revision;
    }

    @Override
    public void advance(String streamKey, long revision) {
        if (mapper.advanceCheckpoint(streamKey, revision) != 1) {
            throw new IllegalStateException("Link 控制检查点推进失败: " + streamKey);
        }
    }

    @Override
    public int apply(LinkControlEvent event) {
        return switch (event) {
            case DomainAvailabilityChanged domain -> mapper.upsertDomainState(
                    domain.domainId(), domain.host(), domain.enabled(), domain.revision()
            );
            case UserLinkSecurityRestrictionsChanged user -> {
                int changed = mapper.deleteUserRestrictions(user.userId());
                if (!user.restrictions().isEmpty()) {
                    changed += mapper.insertUserRestrictions(
                            user.userId(), user.revision(), user.restrictions()
                    );
                }
                yield changed;
            }
        };
    }
}
