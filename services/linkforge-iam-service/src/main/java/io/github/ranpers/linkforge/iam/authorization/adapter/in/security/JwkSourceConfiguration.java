package io.github.ranpers.linkforge.iam.authorization.adapter.in.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.ranpers.linkforge.iam.authorization.application.port.in.LoadSigningKeyUseCase;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningAlgorithm;
import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration(proxyBeanMethods = false)
public class JwkSourceConfiguration {

    @Bean
    @DependsOnDatabaseInitialization
    JWKSource<SecurityContext> jwkSource(LoadSigningKeyUseCase loadSigningKeyUseCase) {
        RSAKey rsaKey = toRsaKey(loadSigningKeyUseCase.loadOrCreate());
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    static RSAKey toRsaKey(SigningKey signingKey) {
        if (signingKey.algorithm() != SigningAlgorithm.RS256) {
            throw new IllegalStateException("不支持的 JWK 算法: " + signingKey.algorithm());
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(signingKey.publicKeyDer()))
            );
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(signingKey.privateKeyDer()))
            );
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(signingKey.keyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("数据库中的 RSA JWK 无法解析", exception);
        }
    }
}
