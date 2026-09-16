package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;

import java.time.Duration;

/**
 * 将已持久化的控制事件发送到消息代理，并同步等待代理确认。
 *
 * <p>接口不负责改变 Outbox 状态；调用方依据成功返回或异常推进发送、重试和
 * parked 状态。</p>
 */
public interface LinkControlEventPublisher {

    /**
     * 在给定时限内发送事件并等待代理确认。
     *
     * @param event 已从 Outbox 锁定的待发送事件
     * @param timeout 等待代理确认的最长时间，必须为正值
     * @throws RuntimeException 序列化、发送或等待确认失败时；具体类型由适配器定义
     */
    void publish(PendingOutboxEvent event, Duration timeout);
}
