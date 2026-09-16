package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.domain.UserId;

public interface UserRoleAssignment {

    void assign(UserId userId, RoleCode roleCode);
}
