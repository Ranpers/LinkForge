package io.github.ranpers.linkforge.link.availability.adapter.out.persistence;

import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationInbox;
import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationStreamCheckpoint;
import io.github.ranpers.linkforge.link.availability.application.port.out.LinkAvailabilityProjection;
import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;
import io.github.ranpers.linkforge.link.availability.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserDomainGrantChanged;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisAuthorizationEventStateStore implements
        AuthorizationInbox,
        AuthorizationStreamCheckpoint,
        LinkAvailabilityProjection {

    private final AuthorizationEventMapper mapper;

    public MybatisAuthorizationEventStateStore(AuthorizationEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean recordIfNew(AuthorizationEvent event) {
        return mapper.insertInbox(
                event.eventId(),
                event.eventType().wireName(),
                event.streamKey()
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
            throw new IllegalStateException("检查点创建后仍不存在: streamKey=" + streamKey);
        }
        return revision;
    }

    @Override
    public void advance(String streamKey, long revision) {
        int rows = mapper.advanceCheckpoint(streamKey, revision);
        if (rows != 1) {
            throw new IllegalStateException(
                    "检查点推进失败: streamKey=" + streamKey + ", revision=" + revision
            );
        }
    }

    @Override
    public int applyTargetState(AuthorizationEvent event) {
        return switch (event) {
            case DomainAvailabilityChanged domainEvent -> mapper.applyDomainAvailability(
                    domainEvent.domainId(),
                    domainEvent.enabled()
            );
            case UserAvailabilityChanged userEvent -> mapper.applyUserAvailability(
                    userEvent.userId(),
                    userEvent.enabled()
            );
            case UserDomainGrantChanged grantEvent -> mapper.applyUserDomainGrant(
                    grantEvent.userId(),
                    grantEvent.domainId(),
                    grantEvent.granted()
            );
        };
    }
}
