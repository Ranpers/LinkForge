package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.GrantProjectionPipeline;
import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.RoleAssignmentFailedException;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final GrantProjectionPipeline pipeline = mock(GrantProjectionPipeline.class);

    @Test
    void shouldFailWhenConfiguredRoleDoesNotExist() {
        UserId userId = new UserId(UUID.randomUUID());
        when(mapper.lockRoleSharedByCode(RoleCode.USER.name())).thenReturn(null);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(pipeline).project(any(Runnable.class), any(), any(Runnable.class));

        var assignment = new MybatisUserRoleAssignment(mapper, pipeline);

        assertThrows(
                RoleAssignmentFailedException.class,
                () -> assignment.assign(userId, RoleCode.USER)
        );
        verify(mapper, never()).findRoleGrantedDomainIds(any());
        verify(mapper, never()).insertByRoleCode(any(), any());
    }

    @Test
    void shouldLockThenCalculateAffectedPairsAndMutate() {
        UserId userId = new UserId(UUID.randomUUID());
        UUID roleId = UUID.randomUUID();
        UUID domainA = UUID.randomUUID();
        UUID domainB = UUID.randomUUID();
        when(mapper.lockRoleSharedByCode(RoleCode.USER.name())).thenReturn(roleId);
        when(mapper.findRoleGrantedDomainIds(RoleCode.USER.name())).thenReturn(List.of(domainA, domainB));
        when(mapper.insertByRoleCode(userId.value(), RoleCode.USER.name())).thenReturn(1);

        AtomicReference<Collection<AffectedPair>> suppliedPairs = new AtomicReference<>(List.of());
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            suppliedPairs.set(invokeAffectedPairs(invocation.getArgument(1)));
            ((Runnable) invocation.getArgument(2)).run();
            return null;
        }).when(pipeline).project(any(Runnable.class), any(), any(Runnable.class));

        var assignment = new MybatisUserRoleAssignment(mapper, pipeline);
        assignment.assign(userId, RoleCode.USER);

        assertEquals(
                Set.of(
                        new AffectedPair(userId.value(), domainA),
                        new AffectedPair(userId.value(), domainB)
                ),
                Set.copyOf(suppliedPairs.get())
        );

        InOrder inOrder = inOrder(mapper);
        inOrder.verify(mapper).lockRoleSharedByCode(RoleCode.USER.name());
        inOrder.verify(mapper).findRoleGrantedDomainIds(RoleCode.USER.name());
        inOrder.verify(mapper).insertByRoleCode(userId.value(), RoleCode.USER.name());
    }

    private static Collection<AffectedPair> invokeAffectedPairs(Object candidate) {
        if (!(candidate instanceof Supplier<?> supplier)) {
            throw new AssertionError("第二个参数必须是受影响集合 Supplier");
        }
        Object supplied = supplier.get();
        if (!(supplied instanceof Collection<?> collection)) {
            throw new AssertionError("Supplier 必须返回 Collection");
        }
        return collection.stream().map(AffectedPair.class::cast).toList();
    }
}
