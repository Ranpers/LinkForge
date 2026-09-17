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

    /**
     * 路由匹配优先级，数值越小越先匹配，先命中者胜出。
     *
     * @implNote 逐条显式给出，不依赖声明顺序。有两条路径同时匹配两条路由：{@code /.well-known/**}
     * 与 {@code /oauth2/jwks} 既匹配 {@code iam-authentication-metadata}，也匹配通配 {@code /oauth2/**}
     * 与 {@code /.well-known/**} 的 {@code iam-authentication}；{@code POST /api/v1/users} 既匹配
     * {@code iam-registration}，也匹配 {@code iam-api}。order 相等时先命中哪一条只取决于声明顺序
     * 能否被稳定保留，挪动一次声明位置就会把精确路由的限流策略静默换成通配路由的：元数据会从可用性
     * 优先变成拒绝降级，注册会用上业务 API 的令牌桶。因此凡有通配匹配之处，精确路由一律取更小的值。
     * 取值只表达相对顺序，留出间隔以便插入新路由。
     */
    private static final int ORDER_IAM_REGISTRATION = 10;

    private static final int ORDER_IAM_AUTHENTICATION_METADATA = 20;

    private static final int ORDER_IAM_AUTHENTICATION = 30;

    private static final int ORDER_IAM_API = 40;

    private static final int ORDER_LINK_API = 50;

    private static final int ORDER_LINK_REDIRECT = 60;

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
    RedisRateLimiter metadataRateLimiter(GatewayRateLimitProperties properties) {
        return rateLimiter(properties.getMetadata());
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
     * @implNote 路由级限流故障策略在这里显式给出，见 {@link RateLimitFailurePolicy}。
     * 注册与认证路由取 {@code REJECT}，避免限流失效后扩大口令爆破风险；IAM 与 Link 管理 API
     * 同样取 {@code REJECT}，避免批量写入、权限探测与资源滥用 —— 它们都要过认证与权限校验，
     * 且含写操作，会消耗数据库、缓存与服务间调用，而客户端通常可以安全重试，因此短时 503
     * 比无保护放行更稳妥。元数据与跳转路由取 {@code ALLOW}，在限流基础设施故障时优先维持可用性：
     * 前者是公开、只读、可缓存的 OIDC 与 JWK 元数据，不参与凭据校验，也没有口令爆破面，让它随
     * 存储一起不可用会波及所有依赖它的资源服务器与 OIDC 客户端；后者确实有流量洪泛、短码枚举与
     * 下游容量风险，这里是自觉地以可用性优先。
     * <p>
     * 策略按路由声明，不能按路径前缀推断。{@code iam-authentication-metadata} 就是按这条规则从
     * {@code iam-authentication} 中拆出来的：一条路由下混着可缓存的公开元数据与要防爆破的令牌
     * 接口时，只能各自成路由。将来若管理 API 下出现纯公开、只读、以可用性优先的接口，也应照此
     * 拆成独立路由并单独指定策略，而不是沿用该前缀现有的 {@code REJECT}。
     * <p>
     * 元数据路由只匹配 GET。Spring Authorization Server 的 Discovery 与 JWK Set 由 GET-only
     * 过滤器处理（{@code NimbusJwkSetEndpointFilter} 与 {@code OidcProviderConfigurationEndpointFilter}
     * 都以 {@code HttpMethod.GET} 构造请求匹配器），不存在 MVC 那种 GET 处理器自动承接 HEAD 的语义，
     * 其他方法即使放行到下游也不会被这两个端点响应，因此不必纳入可用性优先的范围。
     * 公开契约与健康检查不由路由提供，而是网关本地处理，因此不在限流范围内。
     */
    @Bean
    RouteLocator linkForgeRoutes(
            RouteLocatorBuilder builder,
            KeyResolver gatewayClientKeyResolver,
            @Qualifier("authenticationRateLimiter") RedisRateLimiter authenticationRateLimiter,
            @Qualifier("metadataRateLimiter") RedisRateLimiter metadataRateLimiter,
            @Qualifier("apiRateLimiter") RedisRateLimiter apiRateLimiter,
            @Qualifier("redirectRateLimiter") RedisRateLimiter redirectRateLimiter,
            GatewayProblemWriter problemWriter,
            GatewayRateLimitMetrics rateLimitMetrics
    ) {
        requireInspectableDegradation(authenticationRateLimiter);
        requireInspectableDegradation(metadataRateLimiter);
        requireInspectableDegradation(apiRateLimiter);
        requireInspectableDegradation(redirectRateLimiter);

        GatewayFilter authenticationLimit = rateLimit(
                authenticationRateLimiter,
                gatewayClientKeyResolver,
                problemWriter,
                rateLimitMetrics,
                RateLimitFailurePolicy.REJECT
        );
        GatewayFilter metadataLimit = rateLimit(
                metadataRateLimiter,
                gatewayClientKeyResolver,
                problemWriter,
                rateLimitMetrics,
                RateLimitFailurePolicy.ALLOW
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
                        .order(ORDER_IAM_REGISTRATION)
                        .path("/api/v1/users")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(filters -> filters.filter(authenticationLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("iam-authentication-metadata", route -> route
                        .order(ORDER_IAM_AUTHENTICATION_METADATA)
                        .path("/.well-known/**", "/oauth2/jwks")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(filters -> filters.filter(metadataLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("iam-authentication", route -> route
                        .order(ORDER_IAM_AUTHENTICATION)
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
                        .order(ORDER_IAM_API)
                        .path(
                                "/api/v1/users",
                                "/api/v1/users/**",
                                "/api/v1/domains/**"
                        )
                        .filters(filters -> filters.filter(apiLimit))
                        .uri("lb://linkforge-iam-service"))
                .route("link-api", route -> route
                        .order(ORDER_LINK_API)
                        .path("/api/v1/links/**", "/api/v1/groups", "/api/v1/groups/**")
                        .filters(filters -> filters.filter(apiLimit))
                        .uri("lb://linkforge-link-service"))
                .route("link-redirect", route -> route
                        .order(ORDER_LINK_REDIRECT)
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
     * 关掉响应头或改写头名称都会让降级无法识别，拒绝降级的路由会静默退化成放行降级，也就是保护
     * 消失而无人报警，放行路由也不再计入降级指标。宁可让配置错误在启动时暴露，也不要留到运行期才发现。
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
