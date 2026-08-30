package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.grant.application.GrantBatchEngine;
import io.github.ranpers.linkforge.iam.grant.application.GrantChangePlan;
import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.RoleAssignmentFailedException;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRoleAssignment;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.springframework.stereotype.Repository;

/**
 * t_user_role 是授权投影管道的第一条变更面:角色分配必须经管道,
 * 使角色已绑定的域名对新用户即时生效并产生 UserDomainGrantChanged 事件。
 */
@Repository
public class MybatisUserRoleAssignment implements UserRoleAssignment {

    private final UserRoleMapper userRoleMapper;
    private final GrantBatchEngine grantBatchEngine;

    public MybatisUserRoleAssignment(UserRoleMapper userRoleMapper, GrantBatchEngine grantBatchEngine) {
        this.userRoleMapper = userRoleMapper;
        this.grantBatchEngine = grantBatchEngine;
    }

    @Override
    public void assign(UserId userId, RoleCode roleCode) {
        grantBatchEngine.execute(new GrantChangePlan(
                () -> {
                    if (userRoleMapper.lockRoleSharedByCode(roleCode.name()) == null) {
                        throw new RoleAssignmentFailedException(roleCode);
                    }
                },
                () -> userRoleMapper.stageGrantedDomainsForAssignment(userId.value(), roleCode.name()),
                () -> {
                    int affectedRows = userRoleMapper.insertByRoleCode(userId.value(), roleCode.name());
                    if (affectedRows != 1) {
                        throw new RoleAssignmentFailedException(roleCode);
                    }
                }
        ));
    }
}
