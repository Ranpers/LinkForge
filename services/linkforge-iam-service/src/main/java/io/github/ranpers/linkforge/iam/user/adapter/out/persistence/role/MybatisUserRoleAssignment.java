package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.GrantProjectionPipeline;
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
    private final GrantProjectionPipeline projectionPipeline;

    public MybatisUserRoleAssignment(UserRoleMapper userRoleMapper, GrantProjectionPipeline projectionPipeline) {
        this.userRoleMapper = userRoleMapper;
        this.projectionPipeline = projectionPipeline;
    }

    @Override
    public void assign(UserId userId, RoleCode roleCode) {
        projectionPipeline.project(
                () -> {
                    if (userRoleMapper.lockRoleSharedByCode(roleCode.name()) == null) {
                        throw new RoleAssignmentFailedException(roleCode);
                    }
                },
                () -> userRoleMapper.findRoleGrantedDomainIds(roleCode.name()).stream()
                        .map(domainId -> new AffectedPair(userId.value(), domainId))
                        .toList(),
                () -> {
                    int affectedRows = userRoleMapper.insertByRoleCode(userId.value(), roleCode.name());
                    if (affectedRows != 1) {
                        throw new RoleAssignmentFailedException(roleCode);
                    }
                }
        );
    }
}
