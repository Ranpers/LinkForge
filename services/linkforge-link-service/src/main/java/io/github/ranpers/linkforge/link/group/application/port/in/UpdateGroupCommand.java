package io.github.ranpers.linkforge.link.group.application.port.in;

import java.util.UUID;

/**
 * 修改分组的入参。
 *
 * @param ownerUserId 分组归属用户，必须取自访问令牌的 subject
 * @param groupId 目标分组标识
 * @param name 新的分组名称，必填；只调整排序时也需回传当前名称
 * @param sortOrder 新的展示排序提示；为 {@code null} 表示保留原值
 */
public record UpdateGroupCommand(UUID ownerUserId, UUID groupId, String name, Integer sortOrder) {
}
