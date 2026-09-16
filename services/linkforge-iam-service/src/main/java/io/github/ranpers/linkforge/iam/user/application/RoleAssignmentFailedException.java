package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;

/** 数据库缺少目标角色等服务端配置错误。 */
public class RoleAssignmentFailedException extends RuntimeException {

    public RoleAssignmentFailedException(RoleCode roleCode) {
        super("无法分配角色，角色配置不存在: " + roleCode.name());
    }
}
