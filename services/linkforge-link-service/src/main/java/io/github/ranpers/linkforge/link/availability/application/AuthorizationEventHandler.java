package io.github.ranpers.linkforge.link.availability.application;

import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationInbox;
import io.github.ranpers.linkforge.link.availability.application.port.out.AuthorizationStreamCheckpoint;
import io.github.ranpers.linkforge.link.availability.application.port.out.LinkAvailabilityProjection;
import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Link 授权状态投影的事务编排。
 *
 * <p>Inbox、检查点行锁、位更新和检查点推进必须处于同一事务。Kafka offset 不参与
 * 数据库事务；数据库提交后进程崩溃产生的重复消息由 Inbox 拦截。</p>
 */
@Service
public class AuthorizationEventHandler {

    private final AuthorizationInbox inbox;
    private final AuthorizationStreamCheckpoint checkpoint;
    private final LinkAvailabilityProjection projection;

    public AuthorizationEventHandler(
            AuthorizationInbox inbox,
            AuthorizationStreamCheckpoint checkpoint,
            LinkAvailabilityProjection projection
    ) {
        this.inbox = inbox;
        this.checkpoint = checkpoint;
        this.projection = projection;
    }

    @Transactional
    public AuthorizationEventHandlingResult handle(AuthorizationEvent event) {
        if (!inbox.recordIfNew(event)) {
            return AuthorizationEventHandlingResult.duplicate();
        }

        checkpoint.ensureExists(event.streamKey());
        long lastAppliedRevision = checkpoint.lockAndGetRevision(event.streamKey());
        if (event.revision() <= lastAppliedRevision) {
            return AuthorizationEventHandlingResult.stale();
        }

        int updatedLinks = projection.applyTargetState(event);
        checkpoint.advance(event.streamKey(), event.revision());
        return AuthorizationEventHandlingResult.applied(updatedLinks);
    }
}
