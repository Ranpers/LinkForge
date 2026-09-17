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
     *
     * @implNote 路由级限流故障策略在这里显式给出，见 {@link RateLimitFailurePolicy}。注册、认证与
     * 业务管理路由取 {@code REJECT}：相比短时降低可用性，失去这些接口的请求保护、扩大口令爆破与
     * 越权尝试面是更不可接受的风险。跳转路由取 {@code ALLOW}：它同样有流量洪泛、短码枚举与下游
     * 容量风险，被限流保护的正是这些，这里选择可用性优先，因为拒绝降级会让全部短链同时失效。
     * 公开契约与健康检查不由路由提供，而是网关本地处理，因此不在限流范围内。
     */
    @Bean
    RouteLocator linkForgeRoutes(
            RouteLocatorBuilder builder,
            KeyResolver gatewayClientKeyResolver,
            @Qualifier("authenticationRateLimiter") RedisRateLimiter authenticationRateLimiter,
            @Qualifier("apiRateLimiter") RedisRateLimiter apiRateLimiter,
            @Qualifier("redirectRateLimiter") RedisRateLimiter redirectRateLimiter,
            GatewayProblemWriter problemWriter,
            GatewayRateLimitMetrics rateLimitMetrics
    ) {
        requireInspectableDegradation(authenticationRateLimiter);
        requireInspectableDegradation(apiRateLimiter);
        requireInspectableDegradation(redirectRateLimiter);

        GatewayFilter authenticationLimit = rateLimit(
                authenticationRateLimiter,
                gatewayClientKeyResolver,
                problemWriter,
                rateLimitMetrics,
                RateLimitFailurePolicy.REJECT
        );
        GatewayFilter apiLimit = rateLimit(
                apiRateLimiter,
                gatewayClientKeyResolver,
                problemWriter,
                rateLimitMetrics,
                RateLimitFailurePolicy.REJECT
        );
        GatewayFilter redirectLimit = rateLimit(
                redirectRateLimiter,
                gatewayClientKeyResolver,
                problemWriter,
                rateLimitMetrics,
                RateLimitFailurePolicy.ALLOW
        );

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

    /**
     * 断言限流器的降级标记仍可被识别，否则拒绝启动。
     *
     * @param rateLimiter 待检查的限流器
     * @throws IllegalStateException 响应头已被关闭或重命名时
     * @implNote {@link RateLimitDecision} 依赖框架在降级时写出的 {@code X-RateLimit-Remaining: -1}。
     * 关掉响应头或改写头名称都会让降级无法识别，认证与业务管理路由会从拒绝降级静默退化成放行降级，
     * 也就是保护消失而无人报警。宁可让配置错误在启动时暴露，也不要留到运行期才发现。
     * <p>
     * 检查放在这里而不是 {@link #rateLimiter} 里：{@code RedisRateLimiter} 自身带
     * {@code @ConfigurationProperties}，属性绑定发生在本方法返回之后，构造点上的断言看不到
     * {@code spring.cloud.gateway.redis-rate-limiter.*} 的影响。
     */
    private static void requireInspectableDegradation(RedisRateLimiter rateLimiter) {
        if (!rateLimiter.isIncludeHeaders()) {
            throw new IllegalStateException(
                    "限流器必须保留响应头：include-headers 为 false 时降级标记不可见，"
                            + "拒绝降级会静默退化成放行降级"
            );
        }
        if (!RedisRateLimiter.REMAINING_HEADER.equals(rateLimiter.getRemainingHeader())) {
            throw new IllegalStateException(
                    "剩余令牌响应头必须保持默认名称 " + RedisRateLimiter.REMAINING_HEADER
                            + "，当前为 " + rateLimiter.getRemainingHeader()
                            + "：改名后降级标记不可见，拒绝降级会静默退化成放行降级"
            );
        }
    }

    private static GatewayFilter rateLimit(
            RateLimiter<?> rateLimiter,
            KeyResolver keyResolver,
            GatewayProblemWriter problemWriter,
            GatewayRateLimitMetrics metrics,
            RateLimitFailurePolicy failurePolicy
    ) {
        return new GatewayRateLimitFilter(
                rateLimiter,
                keyResolver,
                problemWriter,
                metrics,
                failurePolicy
        );
    }
}
