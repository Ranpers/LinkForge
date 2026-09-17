package io.github.ranpers.linkforge.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 按路由的令牌桶判定结果写入问题响应的限流过滤器。
 *
 * @implNote 不能沿用 {@code RequestRateLimiter} 网关过滤器：它在拒绝请求时先设置状态码再结束响应，
 * 响应已经提交，调用方只能收到一个没有响应体的 429。这里改为在写入响应体之前完成判定。
 * <p>
 * 令牌桶依赖的存储不可用时如何处置由构造时传入的 {@link RateLimitFailurePolicy} 决定，过滤器
 * 不自行取舍：公开跳转路由放行，注册、登录与业务管理路由返回 503。判定结果统一来自
 * {@link RateLimitDecision}，过滤器只按枚举分支，不再解读响应头。
 * <p>
 * 降级被放行时仍会把框架给出的四个限流响应头写回响应，其中 {@code X-RateLimit-Remaining} 为 -1，
 * 调用方据此可以分辨“确实没超限”和“限流已降级”。降级被拒绝时相反：那不是一个配额结论，
 * 503 问题响应只带 {@code X-Request-Id}，公开契约因此不必为它声明限流响应头。
 */
public final class GatewayRateLimitFilter implements GatewayFilter {

    private static final String UNKNOWN_ROUTE = "unknown";

    private final RateLimiter<?> rateLimiter;
    private final KeyResolver keyResolver;
    private final GatewayProblemWriter problemWriter;
    private final GatewayRateLimitMetrics metrics;
    private final RateLimitFailurePolicy failurePolicy;

    public GatewayRateLimitFilter(
            RateLimiter<?> rateLimiter,
            KeyResolver keyResolver,
            GatewayProblemWriter problemWriter,
            GatewayRateLimitMetrics metrics,
            RateLimitFailurePolicy failurePolicy
    ) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
        this.problemWriter = problemWriter;
        this.metrics = metrics;
        this.failurePolicy = failurePolicy;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route == null ? UNKNOWN_ROUTE : route.getId();
        // 用 Optional 承载键解析结果，而不能用 switchIfEmpty：放行分支返回的
        // chain.filter(exchange) 本身就是一个空 Mono，会被误判成“未解析出键”。
        return keyResolver.resolve(exchange)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(key -> key
                        .map(resolved -> limit(exchange, chain, routeId, resolved))
                        .orElseGet(() -> {
                            metrics.recordEmptyKey(routeId);
                            return problemWriter.write(
                                    exchange,
                                    HttpStatus.FORBIDDEN,
                                    "ACCESS_DENIED",
                                    "无法确定限流主体"
                            );
                        }));
    }

    private Mono<Void> limit(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String routeId,
            String key
    ) {
        return rateLimiter.isAllowed(routeId, key).flatMap(response -> {
            RateLimitDecision decision = RateLimitDecision.of(response);
            metrics.record(routeId, decision, failurePolicy);
            if (decision == RateLimitDecision.UNAVAILABLE
                    && failurePolicy == RateLimitFailurePolicy.REJECT) {
                return problemWriter.write(
                        exchange,
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "RATE_LIMITER_UNAVAILABLE",
                        "请求保护服务暂时不可用"
                );
            }
            response.getHeaders().forEach((name, value) ->
                    exchange.getResponse().getHeaders().set(name, value));
            if (decision == RateLimitDecision.DENIED) {
                return problemWriter.write(
                        exchange,
                        HttpStatus.TOO_MANY_REQUESTS,
                        "RATE_LIMIT_EXCEEDED",
                        "请求过于频繁"
                );
            }
            return chain.filter(exchange);
        });
    }
}
