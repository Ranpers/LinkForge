package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import io.github.ranpers.linkforge.iam.user.domain.UserStatus;
import io.github.ranpers.linkforge.iam.user.domain.Username;

import java.util.Optional;
import java.util.Set;

public interface LoginUserQuery {

    Optional<LoginUserSnapshot> findByUsername(Username username);

    record LoginUserSnapshot(
            UserId id,
            Username username,
            PasswordHash passwordHash,
            UserStatus status,
            boolean deleted,
            Set<String> roles,
            Set<String> permissions
    ) {

        public LoginUserSnapshot {
            roles = Set.copyOf(roles);
            permissions = Set.copyOf(permissions);
        }
    }
}
