package io.github.ranpers.linkforge.link.group.application.port.out;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;

import java.util.UUID;

/**
 * 分组写侧持久化端口。
 *
 * <p>所有方法都以归属用户为限定条件，端口实现不得提供忽略归属的写入路径。</p>
 */
public interface GroupRepository {

    /**
     * 插入一个活跃分组，标识与时间戳由数据库生成。
     *
     * <p>依赖 {@code ux_group_user_name} 这一部分唯一索引判重，用 {@code ON CONFLICT DO NOTHING}
     * 把并发重名折叠为返回值而非异常，因此调用方所在事务不会被唯一约束失败中止。</p>
     *
     * @param ownerUserId 归属用户
     * @param name 已归一化的分组名称
     * @param sortOrder 展示排序提示
     * @return 新分组标识；该用户的活跃分组中已存在同名分组时返回 {@code null}
     */
    UUID insert(UUID ownerUserId, String name, int sortOrder);

    /**
     * 更新分组名称与排序提示。
     *
     * @param ownerUserId 归属用户
     * @param groupId 目标分组标识
     * @param name 已归一化的新名称
     * @param sortOrder 新排序提示；为 {@code null} 时保留原值
     * @return 成功、重名或目标不存在三态结果
     */
    UpdateResult update(UUID ownerUserId, UUID groupId, String name, Integer sortOrder);

    /**
     * 软删除分组，并在同一事务中解除该用户在此分组下活跃短链的分组归属。
     *
     * <p>实现必须先锁定活动分组行，使删除与引用该分组的新短链写入串行化。</p>
     *
     * @param ownerUserId 归属用户
     * @param groupId 目标分组标识
     * @return 是否命中一行活跃分组；{@code false} 表示分组不存在、已删除或不属于该用户
     */
    boolean softDelete(UUID ownerUserId, UUID groupId);

    /**
     * 读取归属该用户的活跃分组。
     *
     * @param ownerUserId 归属用户
     * @param groupId 目标分组标识
     * @return 分组只读投影；不存在、已软删除或不属于该用户时返回 {@code null}
     */
    GroupView find(UUID ownerUserId, UUID groupId);

    enum UpdateResult {
        UPDATED,
        NAME_CONFLICT,
        NOT_FOUND
    }
}
