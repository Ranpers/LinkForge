package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.RoleAssignmentFailedException;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRoleAssignment;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisUserRoleAssignment implements UserRoleAssignment {

    private final UserRoleMapper userRoleMapper;

    public MybatisUserRoleAssignment(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public void assign(UserId userId, RoleCode roleCode) {
        int affectedRows = userRoleMapper.insertByRoleCode(userId.value(), roleCode.name());
        if (affectedRows != 1) {
            throw new RoleAssignmentFailedException(roleCode);
        }
    }
}
