package io.github.ranpers.linkforge.iam.domain.application.port.out;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;

import java.util.UUID;

/**
 * 域名写侧持久化端口。
 *
 * <p>权限判定在写入语句内部完成，与写入处于同一事务，端口实现不接受调用方已完成鉴权的假设。</p>
 */
public interface DomainStore {

    /**
     * 插入一个启用状态的域名、把该域名直接授权给创建者，并追加一条
     * {@code DomainAvailabilityChanged} 控制事件，使 link-service 建立解析用的状态投影。
     *
     * <p>三件事在同一条语句内完成，避免出现「域名已建但事件未发」的中间态：
     * 缺少事件会让该域名下的短链创建因 link-service 的外键约束失败。</p>
     *
     * @param actorUserId 操作者；需具备 {@code domain:create} 且账号状态正常
     * @param host        已归一化的主机名
     * @param name        可为空的展示名称
     * @param traceId     可为空；非空时写入控制事件
     * @return 创建结果；{@link CreationResult#CREATED} 时 {@link CreationOutcome#created()} 非空
     */
    CreationOutcome create(UUID actorUserId, String host, String name, ControlEventTraceId traceId);

    /**
     * 更新域名的展示名称。
     *
     * @param actorUserId 操作者；需具备 {@code domain:update} 且账号状态正常
     * @param domainId    目标域名标识
     * @param name        新的展示名称；为 {@code null} 表示清空
     * @return 更新、无变化、目标不存在与权限拒绝四态结果
     */
    UpdateResult update(UUID actorUserId, UUID domainId, String name);

    enum CreationResult {
        CREATED,
        HOST_CONFLICT,
        DENIED
    }

    enum UpdateResult {
        UPDATED,
        UNCHANGED,
        NOT_FOUND,
        DENIED
    }

    /**
     * @param result  创建结果
     * @param created 新建域名的只读投影；仅当 {@code result} 为 {@link CreationResult#CREATED} 时非空
     */
    record CreationOutcome(CreationResult result, DomainListItem created) {
    }
}
