package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

/**
 * 在控制事实提交后同步相应的短链运行时缓存。
 */
public interface LinkControlCache {

    /**
     * 注册事务提交后的缓存更新；事务回滚时不得暴露未提交状态。
     *
     * @param event 已应用到本地投影的控制事件
     */
    void projectAfterCommit(LinkControlEvent event);
}
