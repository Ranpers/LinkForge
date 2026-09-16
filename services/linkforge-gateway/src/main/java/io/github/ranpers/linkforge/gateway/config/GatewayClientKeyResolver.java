package io.github.ranpers.linkforge.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * 为网关限流生成稳定且不可由普通客户端伪造的键。
 *
 * <p>已认证请求使用 JWT subject；匿名请求使用直接对端 IP。这里刻意不读取
 * {@code X-Forwarded-For}，避免在没有可信反向代理边界时被伪造绕过限流。</p>
 */
public final class GatewayClientKeyResolver implements KeyResolver {

    private static final String UNKNOWN_CLIENT = "anonymous:unknown";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(Principal::getName)
                .filter(name -> !name.isBlank())
                .map(name -> "subject:" + name)
                .switchIfEmpty(Mono.fromSupplier(() -> remoteAddressKey(exchange)));
    }

    private static String remoteAddressKey(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return UNKNOWN_CLIENT;
        }
        return "address:" + remoteAddress.getAddress().getHostAddress();
    }
}
