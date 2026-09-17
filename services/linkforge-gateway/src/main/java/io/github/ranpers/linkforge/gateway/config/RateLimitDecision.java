package io.github.ranpers.linkforge.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;

/**
 * 一次路由限流判定的结果。
 *
 * @apiNote 判定结果是与框架解耦的枚举。调用方按它选择处置方式，不必再解读
 * {@link RateLimiter.Response} 的响应头，故障策略也因此可以只依赖本枚举。
 */
public enum RateLimitDecision {

    /** 令牌桶判定放行，且判定本身可信。 */
    ALLOWED,

    /** 令牌桶完成了判定并判定拒绝，请求超出配额。 */
    DENIED,

    /** 确认未能完成判定：令牌桶依赖的存储不可用。 */
    UNAVAILABLE,

    /** 无法确认判定是否可信：降级标记缺失、无法解析，或取值不是已知的那一个。 */
    UNKNOWN;

    /**
     * 剩余令牌数取值，框架用它表示本次判定已经降级。
     */
    private static final long DEGRADED_TOKENS = -1L;

    /**
     * 从框架的限流响应解析判定结果。
     *
     * @param response 令牌桶返回的响应
     * @return 对应的判定结果
     * @implNote {@link RedisRateLimiter} 用剩余令牌数 {@code -1} 标记自身的降级：Redis 调用失败时
     * 它记录错误后返回放行，并把剩余量写成 -1，响应式错误与同步异常两条路径都如此。令牌桶的 Lua
     * 脚本返回的是扣减后的令牌数，恒不为负，因此 -1 只可能来自降级。
     * <p>
     * 这里刻意不把「取不到 -1」当作放行：只有非负的剩余量才算判定可信，标记缺失、无法解析或其他
     * 负值一律归入 {@link #UNKNOWN}，与 {@link #UNAVAILABLE} 同样交由故障策略处置。否则一旦响应头
     * 被关闭、被改名，或框架换掉降级实现，认证与业务管理路由会在无人察觉的情况下从拒绝降级退化成
     * 放行降级 —— 而放行正是攻击者想要的默认结果。
     * {@link GatewayRouteConfiguration} 在启动时断言响应头仍然可用，是这条规则的另一半。
     */
    static RateLimitDecision of(RateLimiter.Response response) {
        if (!response.isAllowed()) {
            return DENIED;
        }
        String remaining = response.getHeaders().get(RedisRateLimiter.REMAINING_HEADER);
        if (remaining == null) {
            return UNKNOWN;
        }
        long tokens;
        try {
            tokens = Long.parseLong(remaining);
        }
        catch (NumberFormatException exception) {
            return UNKNOWN;
        }
        if (tokens >= 0L) {
            return ALLOWED;
        }
        return tokens == DEGRADED_TOKENS ? UNAVAILABLE : UNKNOWN;
    }

    /**
     * 判定是否已经降级，即结果不足以断言请求未超出配额。
     *
     * @return {@link #UNAVAILABLE} 与 {@link #UNKNOWN} 返回 {@code true}
     */
    boolean isDegraded() {
        return this == UNAVAILABLE || this == UNKNOWN;
    }
}
