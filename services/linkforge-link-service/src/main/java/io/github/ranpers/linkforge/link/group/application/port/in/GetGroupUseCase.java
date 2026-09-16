package io.github.ranpers.linkforge.link.group.application.port.in;

import java.util.UUID;

/** 读取单个分组的入站端口。 */
public interface GetGroupUseCase {

    /**
     * 读取指定分组的详情。
     *
     * @param ownerUserId 分组归属用户，必须取自访问令牌的 subject
     * @param groupId 目标分组标识
     * @return 分组只读投影
     * @throws io.github.ranpers.linkforge.link.group.application.GroupNotFoundException 分组不存在、已软删除或不属于该用户
     */
    GroupView get(UUID ownerUserId, UUID groupId);
}
