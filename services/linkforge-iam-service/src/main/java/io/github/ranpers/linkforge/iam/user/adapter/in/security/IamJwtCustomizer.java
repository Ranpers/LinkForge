package io.github.ranpers.linkforge.iam.user.adapter.in.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 为访问令牌写入 API 受众，并为用户令牌写入稳定主体和授权声明。
 */
@Component
public final class IamJwtCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final String accessTokenAudience;

    public IamJwtCustomizer(
            @Value("${linkforge.security.access-token-audience}") String accessTokenAudience
    ) {
        if (accessTokenAudience == null || accessTokenAudience.isBlank()) {
            throw new IllegalArgumentException("访问令牌受众不能为空");
        }
        this.accessTokenAudience = accessTokenAudience;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        boolean accessToken = OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType());
        if (accessToken) {
            context.getClaims().audience(List.of(accessTokenAudience));
        }

        Authentication authentication = context.getPrincipal();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof IamUserPrincipal principal)) {
            return;
        }

        context.getClaims().subject(principal.userId().toString());
        context.getClaims().claim("preferred_username", principal.getUsername());
        if (!accessToken) {
            return;
        }
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .toList();
        List<String> permissions = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .sorted()
                .toList();
        context.getClaims().claim("roles", roles);
        context.getClaims().claim("permissions", permissions);
    }
}
