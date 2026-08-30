package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.grant.application.GrantBatchEngine;
import io.github.ranpers.linkforge.iam.grant.application.GrantBatchResult;
import io.github.ranpers.linkforge.iam.grant.application.GrantChangePlan;
import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.RoleAssignmentFailedException;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MybatisUserRoleAssignmentTest {

    private final UserRoleMapper mapper = mock(UserRoleMapper.class);
    private final GrantBatchEngine engine = mock(GrantBatchEngine.class);

    @Test
    void shouldFailWhenConfiguredRoleDoesNotExist() {
        UserId userId = new UserId(UUID.randomUUID());
        when(mapper.lockRoleSharedByCode(RoleCode.USER.name())).thenReturn(null);
        invokePlanWhenEngineExecutes();

        var assignment = new MybatisUserRoleAssignment(mapper, engine);

        assertThrows(
                RoleAssignmentFailedException.class,
                () -> assignment.assign(userId, RoleCode.USER)
        );
        verify(mapper, never()).stageGrantedDomainsForAssignment(any(), any());
        verify(mapper, never()).insertByRoleCode(any(), any());
    }

    @Test
    void shouldLockThenStageAffectedPairsAndMutate() {
        UserId userId = new UserId(UUID.randomUUID());
        when(mapper.lockRoleSharedByCode(RoleCode.USER.name())).thenReturn(UUID.randomUUID());
        when(mapper.stageGrantedDomainsForAssignment(userId.value(), RoleCode.USER.name())).thenReturn(2);
        when(mapper.insertByRoleCode(userId.value(), RoleCode.USER.name())).thenReturn(1);
        invokePlanWhenEngineExecutes();

        var assignment = new MybatisUserRoleAssignment(mapper, engine);
        assignment.assign(userId, RoleCode.USER);

        InOrder inOrder = inOrder(mapper);
        inOrder.verify(mapper).lockRoleSharedByCode(RoleCode.USER.name());
        inOrder.verify(mapper).stageGrantedDomainsForAssignment(userId.value(), RoleCode.USER.name());
        inOrder.verify(mapper).insertByRoleCode(userId.value(), RoleCode.USER.name());
    }

    private void invokePlanWhenEngineExecutes() {
        doAnswer(invocation -> {
            GrantChangePlan plan = invocation.getArgument(0);
            plan.topologyLock().acquire();
            int affected = plan.impactStaging().stage();
            plan.bindingMutation().apply();
            return new GrantBatchResult(affected, 0);
        }).when(engine).execute(any(GrantChangePlan.class));
    }
}
