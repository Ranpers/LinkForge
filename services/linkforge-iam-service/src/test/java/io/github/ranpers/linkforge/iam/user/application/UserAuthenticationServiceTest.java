package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.user.application.port.out.LoginUserQuery;
import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import io.github.ranpers.linkforge.iam.user.domain.UserStatus;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAuthenticationServiceTest {

    @Test
    void shouldDisableDeletedDatabaseUser() {
        LoginUserQuery query = mock(LoginUserQuery.class);
        Username username = new Username("alice_01");
        when(query.findByUsername(username)).thenReturn(Optional.of(
                new LoginUserQuery.LoginUserSnapshot(
                        new UserId(UUID.randomUUID()),
                        username,
                        new PasswordHash("{bcrypt}encoded"),
                        UserStatus.ACTIVE,
                        true,
                        Set.of("USER"),
                        Set.of("link:create")
                )
        ));

        var loginUser = new UserAuthenticationService(query)
                .loadByUsername("alice_01")
                .orElseThrow();

        assertFalse(loginUser.enabled());
        assertTrue(loginUser.roles().contains("USER"));
        assertTrue(loginUser.permissions().contains("link:create"));
    }

    @Test
    void shouldTreatMalformedUsernameAsMissingUser() {
        LoginUserQuery query = mock(LoginUserQuery.class);

        assertTrue(new UserAuthenticationService(query)
                .loadByUsername("bad user")
                .isEmpty());
    }
}
