package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

/**
 * 通过写屏障使控制事实的事务提交与短链运行时缓存保持一致。
 */
public interface LinkControlCache {

    /**
     * 立即建立缓存写屏障，并注册事务完成后的缓存更新或屏障撤销。
     *
     * @param event 已应用到本地投影的控制事件
     * @throws io.github.ranpers.linkforge.link.resolution.application.port.out.RuntimeCacheMutationException
     *         无法确认写屏障已经建立时
     */
    void synchronizeMutation(LinkControlEvent event);
}
