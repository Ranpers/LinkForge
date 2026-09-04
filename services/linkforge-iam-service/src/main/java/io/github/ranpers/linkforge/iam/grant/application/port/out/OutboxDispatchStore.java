package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 在一个投递事务内锁定并推进 Outbox 事件状态。
 *
 * @apiNote {@link #lockDueRows(int)} 获取的行锁必须保持到调用方事务结束；其余状态
 * 变更方法必须参与同一事务。
 */
public interface OutboxDispatchStore {

    /**
     * 按创建顺序锁定当前可投递的事件，并跳过已被其他投递者锁定的行。
     *
     * @param limit 本批次最多锁定的事件数，必须大于零
     * @return 不超过 {@code limit} 的待投递事件；没有到期事件时返回空列表
     */
    List<PendingOutboxEvent> lockDueRows(int limit);

    /**
     * 将事件标记为已成功发送。
     *
     * @param eventId 已获得代理确认的事件
     */
    void markSent(UUID eventId);

    /**
     * 记录失败并安排下一次投递。
     *
     * @param eventId 发送失败的事件
     * @param retryCount 包含本次失败的累计重试次数
     * @param delay 从当前数据库时间起计算的重试延迟
     * @param lastError 已截断、可安全持久化的错误摘要
     */
    void scheduleRetry(UUID eventId, int retryCount, Duration delay, String lastError);

    /**
     * 将达到最大尝试次数的事件移出自动重试队列。
     *
     * @param eventId 无法继续自动重试的事件
     * @param retryCount 包含本次失败的累计重试次数
     * @param lastError 已截断、可安全持久化的错误摘要
     */
    void park(UUID eventId, int retryCount, String lastError);
}
