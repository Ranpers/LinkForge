package io.github.ranpers.linkforge.link.control.application;

import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCheckpoint;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCache;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlInbox;
import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlProjection;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkControlEventHandler {

    private final LinkControlInbox inbox;
    private final LinkControlCheckpoint checkpoint;
    private final LinkControlProjection projection;
    private final LinkControlCache cache;

    public LinkControlEventHandler(
            LinkControlInbox inbox,
            LinkControlCheckpoint checkpoint,
            LinkControlProjection projection,
            LinkControlCache cache
    ) {
        this.inbox = inbox;
        this.checkpoint = checkpoint;
        this.projection = projection;
        this.cache = cache;
    }

    @Transactional
    public LinkControlEventHandlingResult handle(LinkControlEvent event) {
        if (!inbox.recordIfNew(event)) {
            return LinkControlEventHandlingResult.duplicate();
        }
        checkpoint.ensureExists(event.streamKey());
        long currentRevision = checkpoint.lockAndGetRevision(event.streamKey());
        if (event.revision() <= currentRevision) {
            return LinkControlEventHandlingResult.stale();
        }
        int changedRows = projection.apply(event);
        checkpoint.advance(event.streamKey(), event.revision());
        cache.projectAfterCommit(event);
        return LinkControlEventHandlingResult.applied(changedRows);
    }
}
