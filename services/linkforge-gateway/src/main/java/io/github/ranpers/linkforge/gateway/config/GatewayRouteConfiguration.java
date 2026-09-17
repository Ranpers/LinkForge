package io.github.ranpers.linkforge.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayRateLimitProperties.class)
public class GatewayRouteConfiguration {

    @Bean
    @Primary
    KeyResolver gatewayClientKeyResolver() {
        return new GatewayClientKeyResolver();
    }

    @Bean
    @Primary
    RedisRateLimiter authenticationRateLimiter(GatewayRateLimitProperties properties) {
        return rateLimiter(properties.getAuthentication());
    }

    @Bean
    RedisRateLimiter apiRateLimiter(GatewayRateLimitProperties properties) {
        return rateLimiter(properties.getApi());
    }

    @Bean
    RedisRateLimiter redirectRateLimiter(GatewayRateLimitProperties properties) {
        return rateLimiter(properties.getRedirect());
    }

    /**
     * 声明唯一允许从公网入口到达的路由。
     *
     * <p>{@code /internal/**} 故意没有路由，IAM 内部授权接口只能由服务网络访问。</p>
     */
    @Bean
    RouteLocator linkForgeRoutes(
            RouteLocatorBuilder builder,
            KeyResolver gatewayClientKeyResolver,
            @Qualifier("authenticationRateLimiter") RedisRateLimiter authenticationRateLimiter,
            @Qualifier("apiRateLimiter") RedisRateLimiter apiRateLimiter,
            @Qualifier("redirectRateLimiter") RedisRateLimiter redirectRateLimiter,
            GatewayProblemWriter problemWriter
    ) {
        GatewayFilter authenticationLimit =
                rateLimit(authenticationRateLimiter, gatewayClientKeyResolver, problemWriter);
        GatewayFilter apiLimit = rateLimit(apiRateLimiter, gatewayClientKeyResolver, problemWriter);
        GatewayFilter redirectLimit =
                rateLimit(redirectRateLimiter, gatewayClientKeyResolver, problemWriter);

        return builder.routes()
                .route("iam-registration", route -> route
                        .path("/api/v1/users")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(filters -> filters.filter(authenticationLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("iam-authentication", route -> route
                        .path(
                                "/oauth2/**",
                                "/.well-known/**",
                                "/userinfo",
                                "/login",
                                "/login/**"
                        )
                        .filters(filters -> filters.filter(authenticationLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("iam-api", route -> route
                        .path(
                                "/api/v1/users",
                                "/api/v1/users/**",
                                "/api/v1/domains/**"
                        )
                        .filters(filters -> filters.filter(apiLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("link-api", route -> route
                        .path("/api/v1/links/**", "/api/v1/groups", "/api/v1/groups/**")
                        .filters(filters -> filters.filter(apiLimit))
                        .uri("lb://linkforge-link-service"))
                .route("link-redirect", route -> route
                        .path("/r/**")
                        .filters(filters -> filters
                                .filter(redirectLimit)
                                .preserveHostHeader())
                        .uri("lb://linkforge-link-service"))
                .build();
    }

    private static RedisRateLimiter rateLimiter(GatewayRateLimitProperties.Limit limit) {
        return new RedisRateLimiter(limit.replenishRate(), limit.burstCapacity());
    }

    private static GatewayFilter rateLimit(
            RateLimiter<?> rateLimiter,
            KeyResolver keyResolver,
            GatewayProblemWriter problemWriter
    ) {
        return new GatewayRateLimitFilter(rateLimiter, keyResolver, problemWriter);
    }
}
