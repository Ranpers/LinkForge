package io.github.ranpers.linkforge.iam.user.adapter.in.security;

import io.github.ranpers.linkforge.iam.user.application.port.in.LoadLoginUserUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.in.LoginUser;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/** Spring Security 入站适配器：把核心登录模型翻译为框架 UserDetails。 */
@Component
public class DatabaseUserDetailsService implements UserDetailsService {

    private final LoadLoginUserUseCase loadLoginUserUseCase;

    public DatabaseUserDetailsService(LoadLoginUserUseCase loadLoginUserUseCase) {
        this.loadLoginUserUseCase = loadLoginUserUseCase;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        LoginUser loginUser = loadLoginUserUseCase.loadByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        loginUser.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
        loginUser.permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return org.springframework.security.core.userdetails.User
                .withUsername(loginUser.username())
                .password(loginUser.passwordHash())
                .disabled(!loginUser.enabled())
                .authorities(authorities)
                .build();
    }
}
