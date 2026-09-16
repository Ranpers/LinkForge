package io.github.ranpers.linkforge.link.group.application.port.in;

import java.util.UUID;

/**
 * 删除分组的入站端口。
 *
 * <p>删除是软删除，并且不级联删除短链：同一次事务中会把该用户在此分组下的活跃短链
 * 的 {@code group_id} 置空。分组行锁会与新短链的共享锁互斥，防止并发创建留下对已删除
 * 分组的引用。</p>
 */
public interface DeleteGroupUseCase {

    /**
     * 软删除分组并解除其下短链的分组归属。
     *
     * @param ownerUserId 分组归属用户，必须取自访问令牌的 subject
     * @param groupId 目标分组标识
     * @throws io.github.ranpers.linkforge.link.group.application.GroupNotFoundException 分组不存在、已软删除或不属于该用户
     */
    void delete(UUID ownerUserId, UUID groupId);
}
