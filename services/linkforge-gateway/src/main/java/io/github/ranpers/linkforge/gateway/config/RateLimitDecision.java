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

    /** 未能完成判定：令牌桶依赖的存储不可用。 */
    UNAVAILABLE;

    /**
     * 剩余令牌数取值，表示本次判定已经降级。
     */
    private static final String DEGRADED_REMAINING = "-1";

    /**
     * 从框架的限流响应解析判定结果。
     *
     * @param response 令牌桶返回的响应
     * @return 对应的判定结果
     * @implNote {@link RedisRateLimiter} 用剩余令牌数 {@code -1} 标记自身的降级：Redis 调用失败时
     * 它记录错误后返回放行，并把剩余量写成 -1，响应式错误与同步异常两条路径都如此。令牌桶的 Lua
     * 脚本返回的是扣减后的令牌数，恒不为负，因此 -1 只可能来自降级，不会与真实的额度耗尽混淆。
     * 代价是本解析依赖限流器保留 {@code X-RateLimit-Remaining} 响应头，即
     * {@link RedisRateLimiter#isIncludeHeaders()} 为真；本项目从不关闭它。
     */
    static RateLimitDecision of(RateLimiter.Response response) {
        if (!response.isAllowed()) {
            return DENIED;
        }
        return DEGRADED_REMAINING.equals(response.getHeaders().get(RedisRateLimiter.REMAINING_HEADER))
                ? UNAVAILABLE
                : ALLOWED;
    }
}
