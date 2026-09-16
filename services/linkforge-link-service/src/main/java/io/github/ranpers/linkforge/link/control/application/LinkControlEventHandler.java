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

    /**
     * 原子地去重事件、拒绝陈旧修订并更新本地控制投影。
     *
     * <p>投影更新后先建立缓存写屏障；只有事务提交后才写入新缓存值。重复事件和低于
     * 或等于当前检查点的事件都会成功消费，但不会再次修改投影。</p>
     *
     * @param event 已完成反序列化和不变量校验的控制事件
     * @return 明确区分已应用、重复和陈旧事件的处理结果
     */
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
        cache.synchronizeMutation(event);
        return LinkControlEventHandlingResult.applied(changedRows);
    }
}
