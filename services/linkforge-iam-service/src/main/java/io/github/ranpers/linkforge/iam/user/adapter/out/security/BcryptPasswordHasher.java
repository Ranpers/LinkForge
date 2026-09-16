package io.github.ranpers.linkforge.iam.user.adapter.out.security;

import io.github.ranpers.linkforge.iam.user.application.port.out.PasswordHasher;
import io.github.ranpers.linkforge.iam.user.domain.PasswordHash;
import io.github.ranpers.linkforge.iam.user.domain.RawPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码散列出站适配器:包装 Spring Security 的 PasswordEncoder(BCrypt,见 SecurityConfig),
 * 领域/应用层只依赖 PasswordHasher 出港接口
 */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        return new PasswordHash(passwordEncoder.encode(rawPassword.value()));
    }
}
