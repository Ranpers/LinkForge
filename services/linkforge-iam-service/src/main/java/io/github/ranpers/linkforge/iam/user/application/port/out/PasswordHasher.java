package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.RawPassword;

/**
 * 出港(driven port):密码散列抽象。核心层不知道 BCrypt 与 Spring Security 的存在
 */
public interface PasswordHasher {

    PasswordHash hash(RawPassword rawPassword);
}
