package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.auth;

import io.github.ranpers.linkforge.iam.user.application.port.out.LoginUserQuery;
import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import io.github.ranpers.linkforge.iam.user.domain.UserStatus;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.Optional;

@Repository
public class MybatisLoginUserQuery implements LoginUserQuery {

    private final LoginUserMapper loginUserMapper;

    public MybatisLoginUserQuery(LoginUserMapper loginUserMapper) {
        this.loginUserMapper = loginUserMapper;
    }

    @Override
    public Optional<LoginUserSnapshot> findByUsername(Username username) {
        LoginUserRow row = loginUserMapper.findByUsername(username.value());
        if (row == null) {
            return Optional.empty();
        }

        return Optional.of(new LoginUserSnapshot(
                new UserId(row.getId()),
                Username.of(row.getUsername()),
                new PasswordHash(row.getPassword()),
                UserStatus.fromDatabaseValue(row.getStatus()),
                row.getDeletedAt() != null,
                new LinkedHashSet<>(loginUserMapper.findRoleCodes(row.getId())),
                new LinkedHashSet<>(loginUserMapper.findPermissionCodes(row.getId()))
        ));
    }
}
