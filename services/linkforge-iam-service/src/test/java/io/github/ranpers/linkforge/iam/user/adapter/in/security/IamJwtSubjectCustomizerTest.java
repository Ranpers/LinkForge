package io.github.ranpers.linkforge.iam.user.adapter.in.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IamJwtSubjectCustomizerTest {

    @Test
    void shouldUseStableUserIdAsJwtSubject() {
        UUID userId = UUID.randomUUID();
        IamUserPrincipal principal = new IamUserPrincipal(
                userId,
                "alice_01",
                "{bcrypt}encoded",
                true,
                List.of()
        );
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        JwtEncodingContext context = JwtEncodingContext
            .with(JwsHeader.with(SignatureAlgorithm.RS256), claims)
            .principal(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()))
            .build();

        new IamJwtSubjectCustomizer().customize(context);

        assertEquals(userId.toString(), claims.build().getSubject());
    }
}
