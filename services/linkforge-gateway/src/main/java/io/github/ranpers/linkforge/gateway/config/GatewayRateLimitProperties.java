package io.github.ranpers.linkforge.gateway.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "linkforge.gateway.rate-limit")
public class GatewayRateLimitProperties {

    @Valid
    @NotNull
    private Limit authentication = new Limit(10, 20);

    @Valid
    @NotNull
    private Limit api = new Limit(30, 60);

    @Valid
    @NotNull
    private Limit redirect = new Limit(100, 200);

    public Limit getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Limit authentication) {
        this.authentication = authentication;
    }

    public Limit getApi() {
        return api;
    }

    public void setApi(Limit api) {
        this.api = api;
    }

    public Limit getRedirect() {
        return redirect;
    }

    public void setRedirect(Limit redirect) {
        this.redirect = redirect;
    }

    /**
     * 单路由令牌桶限制。
     *
     * @param replenishRate 每秒补充的令牌数
     * @param burstCapacity 允许的瞬时桶容量，必须不小于补充速率
     */
    public record Limit(
            @Min(1) int replenishRate,
            @Min(1) int burstCapacity
    ) {
        public Limit {
            if (burstCapacity < replenishRate) {
                throw new IllegalArgumentException("burst-capacity 不能小于 replenish-rate");
            }
        }
    }
}
