package io.github.ranpers.linkforge.link.group.application.port.in;

import java.util.UUID;

/**
 * 创建分组的入参。
 *
 * @param ownerUserId 分组归属用户，必须取自访问令牌的 subject，不接受请求体提供
 * @param name 原始分组名称，由 {@link io.github.ranpers.linkforge.link.group.domain.GroupName} 去除首尾空白后校验
 * @param sortOrder 展示排序提示，数值越小越靠前
 */
public record CreateGroupCommand(UUID ownerUserId, String name, int sortOrder) {
}
