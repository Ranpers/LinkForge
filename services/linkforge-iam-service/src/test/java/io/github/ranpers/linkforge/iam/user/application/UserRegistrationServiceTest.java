package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.port.in.RegisterUserCommand;
import io.github.ranpers.linkforge.iam.user.application.port.out.PasswordHasher;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserIdGenerator;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRepository;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRoleAssignment;
import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.RawPassword;
import io.github.ranpers.linkforge.iam.user.domain.User;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    private static final UserId USER_ID =
            new UserId(UUID.fromString("0198f7c4-3ee6-7000-8000-000000000001"));

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleAssignment userRoleAssignment;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private UserIdGenerator userIdGenerator;

    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new UserRegistrationService(
                userRepository,
                userRoleAssignment,
                passwordHasher,
                userIdGenerator
        );
    }

    @Test
    void shouldSaveUserAndAssignDefaultRole() {
        when(userRepository.existsByUsername(new Username("alice_01"))).thenReturn(false);
        when(passwordHasher.hash(new RawPassword("secret12")))
                .thenReturn(new PasswordHash("{bcrypt}encoded"));
        when(userIdGenerator.nextId()).thenReturn(USER_ID);

        var registered = service.register(
                new RegisterUserCommand("alice_01", "secret12", "Alice@Example.com")
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userRoleAssignment).assign(USER_ID, RoleCode.USER);

        assertEquals(USER_ID.value(), registered.id());
        assertEquals("alice_01", registered.username());
        assertEquals("alice@example.com", registered.email());
        assertEquals("{bcrypt}encoded", userCaptor.getValue().passwordHash().value());
    }

    @Test
    void shouldStopBeforeHashingWhenUsernameExists() {
        Username username = new Username("alice_01");
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> service.register(new RegisterUserCommand("alice_01", "secret12", null))
        );

        verify(passwordHasher, never()).hash(org.mockito.ArgumentMatchers.any(RawPassword.class));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verify(userRoleAssignment, never()).assign(
                org.mockito.ArgumentMatchers.any(UserId.class),
                org.mockito.ArgumentMatchers.any(RoleCode.class)
        );
    }
}
