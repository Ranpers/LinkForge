package io.github.ranpers.linkforge.gateway.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * 路由限流的判定计数。
 *
 * <p>指标名为 {@code linkforge.gateway.rate_limit.} 加判定结果，降级放行与无法确定限流主体
 * 各有独立计数，因此不必再额外声明结果标签。</p>
 *
 * @implNote 标签只有路由 ID，取值来自网关自己声明的有限路由集合，基数可控。刻意不记录限流主体、
 * 客户端地址、请求 ID 或请求路径：这些取值由外部输入决定，任何一条都会把时间序列数量变成无界，
 * 从而让指标本身成为故障源。
 * @see GatewayRouteConfiguration
 */
@Component
public final class GatewayRateLimitMetrics {

    private static final String PREFIX = "linkforge.gateway.rate_limit.";
    private static final String ROUTE_TAG = "route";

    private final MeterRegistry registry;

    public GatewayRateLimitMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 记录一次限流判定。
     *
     * @param routeId 路由 ID
     * @param decision 判定结果
     * @param failurePolicy 该路由的故障策略；只有降级被放行时才额外累加 {@code fail_open}
     */
    public void record(
            String routeId,
            RateLimitDecision decision,
            RateLimitFailurePolicy failurePolicy
    ) {
        registry.counter(PREFIX + metricName(decision), ROUTE_TAG, routeId).increment();
        if (decision == RateLimitDecision.UNAVAILABLE
                && failurePolicy == RateLimitFailurePolicy.ALLOW) {
            registry.counter(PREFIX + "fail_open", ROUTE_TAG, routeId).increment();
        }
    }

    /**
     * 记录一次无法确定限流主体。
     *
     * @param routeId 路由 ID
     */
    public void recordEmptyKey(String routeId) {
        registry.counter(PREFIX + "empty_key", ROUTE_TAG, routeId).increment();
    }

    private static String metricName(RateLimitDecision decision) {
        return switch (decision) {
            case ALLOWED -> "allowed";
            case DENIED -> "denied";
            case UNAVAILABLE -> "unavailable";
        };
    }
}
