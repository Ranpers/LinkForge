package io.github.ranpers.linkforge.link.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class ResourceServerConfig {

    @Bean
    SecurityFilterChain linkApiSecurity(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/r/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/links")
                        .hasAuthority("SCOPE_link.write")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/links/**")
                        .hasAuthority("SCOPE_link.write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/links/**")
                        .hasAuthority("SCOPE_link.write")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
