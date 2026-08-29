package io.github.ranpers.linkforge.iam.config.jwk;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class PersistentJwkConfiguration {

    @Bean
    @DependsOnDatabaseInitialization
    JWKSource<SecurityContext> jwkSource(PersistentJwkService persistentJwkService) {
        RSAKey rsaKey = persistentJwkService.loadOrCreate();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }
}
