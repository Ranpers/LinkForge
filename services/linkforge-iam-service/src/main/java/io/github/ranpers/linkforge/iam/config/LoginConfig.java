package io.github.ranpers.linkforge.iam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class LoginConfig {
    @Bean
    @Order(3)
    SecurityFilterChain loginChain(HttpSecurity httpSecurity) {
        httpSecurity
                .formLogin(Customizer.withDefaults())       // 提供 /login 页面与提交
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated());     // 其余路径一律要求登录
        return httpSecurity.build();
    }
}
