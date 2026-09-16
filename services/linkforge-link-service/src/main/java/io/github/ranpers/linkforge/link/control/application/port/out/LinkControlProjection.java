package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

/**
 * 将 IAM 控制事件投影为 Link 服务本地的运行时事实。
 */
public interface LinkControlProjection {

    /**
     * 在当前消费事务内应用一个已通过去重和修订检查的事件。
     *
     * @param event 要应用的控制事件
     * @return 被插入、更新或删除的投影行数
     */
    int apply(LinkControlEvent event);
}
