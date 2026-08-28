package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.application.port.in.RegisterUserCommand;
import io.github.ranpers.linkforge.iam.user.application.port.in.RegisteredUser;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserRegistrationUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.out.PasswordHasher;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserIdGenerator;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRepository;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRoleAssignment;
import io.github.ranpers.linkforge.iam.user.domain.EmailAddress;
import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.RawPassword;
import io.github.ranpers.linkforge.iam.user.domain.User;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 注册用例事务边界：保存用户和分配默认角色必须同时成功或同时回滚。
 */
@Service
public class UserRegistrationService implements UserRegistrationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationService.class);

    private final UserRepository userRepository;
    private final UserRoleAssignment userRoleAssignment;
    private final PasswordHasher passwordHasher;
    private final UserIdGenerator userIdGenerator;

    public UserRegistrationService(
            UserRepository userRepository,
            UserRoleAssignment userRoleAssignment,
            PasswordHasher passwordHasher,
            UserIdGenerator userIdGenerator
    ) {
        this.userRepository = userRepository;
        this.userRoleAssignment = userRoleAssignment;
        this.passwordHasher = passwordHasher;
        this.userIdGenerator = userIdGenerator;
    }

    @Transactional
    @Override
    public RegisteredUser register(RegisterUserCommand command) {
        Username username = Username.of(command.username());
        RawPassword rawPassword = RawPassword.of(command.password());
        EmailAddress email = EmailAddress.optional(command.email());

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        PasswordHash passwordHash = passwordHasher.hash(rawPassword);
        User user = User.register(userIdGenerator.nextId(), username, passwordHash, email);

        userRepository.save(user);
        userRoleAssignment.assign(user.id(), RoleCode.USER);

        log.info("用户注册成功: username={}, userId={}", user.username().value(), user.id().value());
        return new RegisteredUser(user.id().value(), user.username().value(), user.email().value());
    }
}
