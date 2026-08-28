package io.github.ranpers.linkforge.iam.user.adapter.out.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserRepository;
import io.github.ranpers.linkforge.iam.user.domain.User;
import io.github.ranpers.linkforge.iam.user.domain.Username;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    public MybatisUserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean existsByUsername(Username username) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username.value()));
        return count != null && count > 0;
    }

    @Override
    public void save(User user) {
        UserDO po = new UserDO();
        po.setId(user.id().value());
        po.setUsername(user.username().value());
        po.setPassword(user.passwordHash().value());
        po.setEmail(user.email().value());
        try {
            userMapper.insert(po);
        } catch (DuplicateKeyException e) {
            if (isUsernameConstraintViolation(e)) {
                throw new UsernameAlreadyExistsException(user.username());
            }
            throw e;
        }
    }

    private boolean isUsernameConstraintViolation(DuplicateKeyException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof PSQLException postgresException
                    && postgresException.getServerErrorMessage() != null) {
                return "t_user_username_key".equals(
                        postgresException.getServerErrorMessage().getConstraint());
            }
            current = current.getCause();
        }
        return false;
    }
}
