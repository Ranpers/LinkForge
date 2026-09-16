package io.github.ranpers.linkforge.iam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 新密码默认使用 BCrypt；委托编码器同时按前缀选择 OAuth 客户端密钥算法。
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
