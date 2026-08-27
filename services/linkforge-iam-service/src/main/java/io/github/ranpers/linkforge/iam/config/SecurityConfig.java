package io.github.ranpers.linkforge.iam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
}
