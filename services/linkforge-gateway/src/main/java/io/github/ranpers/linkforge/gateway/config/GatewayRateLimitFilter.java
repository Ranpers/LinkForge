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
 * 在超出配额时返回问题响应的限流过滤器。
 *
 * @implNote 不能沿用 {@code RequestRateLimiter} 网关过滤器：它在拒绝请求时先设置状态码再结束响应，
 * 响应已经提交，调用方只能收到一个没有响应体的 429。这里改为在写入响应体之前完成判定。
 */
public final class GatewayRateLimitFilter implements GatewayFilter {

    private static final String UNKNOWN_ROUTE = "unknown";

    private final RateLimiter<?> rateLimiter;
    private final KeyResolver keyResolver;
    private final GatewayProblemWriter problemWriter;

    public GatewayRateLimitFilter(
            RateLimiter<?> rateLimiter,
            KeyResolver keyResolver,
            GatewayProblemWriter problemWriter
    ) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
        this.problemWriter = problemWriter;
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
                        .orElseGet(() -> problemWriter.write(
                                exchange,
                                HttpStatus.FORBIDDEN,
                                "ACCESS_DENIED",
                                "无法确定限流主体"
                        )));
    }

    private Mono<Void> limit(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String routeId,
            String key
    ) {
        return rateLimiter.isAllowed(routeId, key).flatMap(response -> {
            response.getHeaders().forEach((name, value) ->
                    exchange.getResponse().getHeaders().set(name, value));
            if (response.isAllowed()) {
                return chain.filter(exchange);
            }
            return problemWriter.write(
                    exchange,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMIT_EXCEEDED",
                    "请求过于频繁"
            );
        });
    }
}
