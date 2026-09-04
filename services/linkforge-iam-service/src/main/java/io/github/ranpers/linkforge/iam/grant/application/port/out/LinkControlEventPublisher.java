package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;

import java.time.Duration;

/** 同步等待消息代理确认;失败时抛异常,由应用服务推进 Outbox 重试状态。 */
public interface LinkControlEventPublisher {

    void publish(PendingOutboxEvent event, Duration timeout);
}
