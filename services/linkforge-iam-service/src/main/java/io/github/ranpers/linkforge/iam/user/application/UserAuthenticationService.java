package io.github.ranpers.linkforge.iam.user.application;

import io.github.ranpers.linkforge.iam.user.application.port.in.LoadLoginUserUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.in.LoginUser;
import io.github.ranpers.linkforge.iam.user.application.port.out.LoginUserQuery;
import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import io.github.ranpers.linkforge.iam.user.domain.UserStatus;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserAuthenticationService implements LoadLoginUserUseCase {

    private final LoginUserQuery loginUserQuery;

    public UserAuthenticationService(LoginUserQuery loginUserQuery) {
        this.loginUserQuery = loginUserQuery;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LoginUser> loadByUsername(String rawUsername) {
        final Username username;
        try {
            username = Username.of(rawUsername);
        } catch (InvalidUserDataException ignored) {
            return Optional.empty();
        }

        return loginUserQuery.findByUsername(username).map(snapshot -> new LoginUser(
                snapshot.id().value(),
                snapshot.username().value(),
                snapshot.passwordHash().value(),
                snapshot.status() == UserStatus.ACTIVE && !snapshot.deleted(),
                snapshot.roles(),
                snapshot.permissions()
        ));
    }
}
