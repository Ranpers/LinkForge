package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.RoleAssignmentFailedException;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MybatisUserRoleAssignmentTest {

    @Test
    void shouldFailWhenConfiguredRoleDoesNotExist() {
        UserRoleMapper mapper = mock(UserRoleMapper.class);
        UserId userId = new UserId(UUID.randomUUID());
        when(mapper.insertByRoleCode(userId.value(), RoleCode.USER.name())).thenReturn(0);

        var assignment = new MybatisUserRoleAssignment(mapper);

        assertThrows(
                RoleAssignmentFailedException.class,
                () -> assignment.assign(userId, RoleCode.USER)
        );
    }
}
