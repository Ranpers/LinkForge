package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** 用户管理列表的安全只读投影，不包含密码散列。 */
public record UserListItem(
        UUID id,
        String username,
        String email,
        String realName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
