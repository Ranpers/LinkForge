package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

/**
 * 以事件唯一标识记录消费事实，消化 at-least-once 投递产生的重复消息。
 */
public interface LinkControlInbox {

    /**
     * 仅在事件从未处理过时写入 Inbox。
     *
     * @param event 已完成结构和不变量校验的控制事件
     * @return {@code true} 表示首次记录，{@code false} 表示重复事件
     */
    boolean recordIfNew(LinkControlEvent event);
}
