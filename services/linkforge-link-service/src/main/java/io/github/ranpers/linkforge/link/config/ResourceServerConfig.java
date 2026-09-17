package io.github.ranpers.linkforge.link.config;

import io.github.ranpers.linkforge.link.infrastructure.web.ApiSecurityProblemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.LinkedHashSet;

@Configuration(proxyBeanMethods = false)
public class ResourceServerConfig {

    @Bean
    SecurityFilterChain linkApiSecurity(
            HttpSecurity http,
            ApiSecurityProblemHandler securityProblemHandler
    ) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
                        .permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/r/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/links/**")
                        .hasAuthority("link:read")
                        .requestMatchers(HttpMethod.POST, "/api/v1/links")
                        .hasAuthority("link:create")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/links/**")
                        .hasAnyAuthority("link:update", "link:manage:any")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/links/**")
                        .hasAnyAuthority("link:delete", "link:manage:any")
                        .requestMatchers(HttpMethod.GET, "/api/v1/groups", "/api/v1/groups/**")
                        .hasAuthority("group:read")
                        .requestMatchers(HttpMethod.POST, "/api/v1/groups")
                        .hasAuthority("group:create")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/groups/**")
                        .hasAuthority("group:update")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/groups/**")
                        .hasAuthority("group:delete")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityProblemHandler)
                        .accessDeniedHandler(securityProblemHandler))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(securityProblemHandler)
                        .accessDeniedHandler(securityProblemHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeAuthorities = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> authorities(jwt, scopeAuthorities));
        return converter;
    }

    private static Collection<GrantedAuthority> authorities(
            Jwt jwt,
            JwtGrantedAuthoritiesConverter scopeAuthorities
    ) {
        LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthorities.convert(jwt));
        claimValues(jwt, "roles").stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
        claimValues(jwt, "permissions").stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }

    private static Collection<String> claimValues(Jwt jwt, String claimName) {
        Collection<String> values = jwt.getClaimAsStringList(claimName);
        return values == null ? java.util.List.of() : values;
    }
}
