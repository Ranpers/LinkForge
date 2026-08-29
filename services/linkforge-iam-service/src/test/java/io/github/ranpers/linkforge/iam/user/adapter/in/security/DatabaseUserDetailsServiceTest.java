package io.github.ranpers.linkforge.iam.user.adapter.in.security;

import io.github.ranpers.linkforge.iam.user.application.port.in.LoadLoginUserUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.in.LoginUser;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseUserDetailsServiceTest {

    @Test
    void shouldMapRolesAndPermissionsToSpringAuthorities() {
        LoadLoginUserUseCase useCase = mock(LoadLoginUserUseCase.class);
        UUID userId = UUID.randomUUID();
        when(useCase.loadByUsername("alice_01")).thenReturn(Optional.of(
                new LoginUser(
                        userId,
                        "alice_01",
                        "{bcrypt}encoded",
                        true,
                        Set.of("USER"),
                        Set.of("link:create")
                )
        ));

        var details = new DatabaseUserDetailsService(useCase).loadUserByUsername("alice_01");
        IamUserPrincipal principal = assertInstanceOf(IamUserPrincipal.class, details);

        assertEquals(userId, principal.userId());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_USER")));
        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "link:create")));
        assertFalse(details.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_link:create")));
    }
}
