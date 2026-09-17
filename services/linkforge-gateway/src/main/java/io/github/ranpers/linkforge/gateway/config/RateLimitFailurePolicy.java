package io.github.ranpers.linkforge.gateway.config;

/**
 * 令牌桶无法完成判定时，网关对该路由请求的处置方式。
 *
 * @apiNote 策略在装配路由时显式指定，不从路由 ID 推断。路由 ID 只是运维命名，改名不应改变
 * 安全语义，而由字符串推断出的策略在改名后会静默失效。
 * @see GatewayRouteConfiguration
 */
public enum RateLimitFailurePolicy {

    /**
     * 放行。
     *
     * <p>用于限流只承担削峰的路由：失去限流不会扩大攻击面，而依赖故障时拒绝服务会把
     * 一次存储抖动放大成整条业务不可用。</p>
     */
    ALLOW,

    /**
     * 拒绝并返回 503。
     *
     * <p>用于一旦失去限流就会暴露口令爆破或越权尝试面的路由。此时可用性让位于保护，
     * 返回 503 而不是 429：判定没有完成，没有资格声称对方超了配额。</p>
     */
    REJECT
}
