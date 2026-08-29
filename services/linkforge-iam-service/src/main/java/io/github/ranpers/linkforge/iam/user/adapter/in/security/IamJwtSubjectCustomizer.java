package io.github.ranpers.linkforge.iam.user.adapter.in.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/**
 * 用户 Token 的 subject 使用不可变 UUID，而不是可变用户名。
 */
@Component
public final class IamJwtSubjectCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication authentication = context.getPrincipal();
        if (authentication != null && authentication.getPrincipal() instanceof IamUserPrincipal principal) {
            context.getClaims().subject(principal.userId().toString());
        }
    }
}
