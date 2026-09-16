package io.github.ranpers.linkforge.iam.user.domain;

import java.util.Objects;

/**
 * 用户聚合根。所有创建路径必须经过工厂方法，保证核心不变量不依赖 Web 校验。
 */
@SuppressWarnings("unused")
public final class User {

    private final UserId id;
    private final Username username;
    private final PasswordHash passwordHash;
    private final EmailAddress email;
    private final UserStatus status;

    private User(
            UserId id,
            Username username,
            PasswordHash passwordHash,
            EmailAddress email,
            UserStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.email = Objects.requireNonNull(email, "email");
        this.status = Objects.requireNonNull(status, "status");
    }

    public static User register(
            UserId id,
            Username username,
            PasswordHash passwordHash,
            EmailAddress email
    ) {
        return new User(id, username, passwordHash, email, UserStatus.ACTIVE);
    }

    public static User rehydrate(
            UserId id,
            Username username,
            PasswordHash passwordHash,
            EmailAddress email,
            UserStatus status
    ) {
        return new User(id, username, passwordHash, email, status);
    }

    public UserId id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public EmailAddress email() {
        return email;
    }

    public UserStatus status() {
        return status;
    }
}
