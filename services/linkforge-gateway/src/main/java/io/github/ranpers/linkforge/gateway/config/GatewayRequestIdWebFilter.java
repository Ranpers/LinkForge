package io.github.ranpers.linkforge.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 在安全过滤器之前确定请求关联标识，并传递给下游及调用方。
 *
 * @apiNote 网关是信任边界，因此不沿用调用方送来的取值：每次请求都重新生成，
 * 覆盖任何入站 {@value #HEADER_NAME}。下游服务因此只可能看到网关签发或服务间传递的标识。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class GatewayRequestIdWebFilter implements WebFilter {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String ATTRIBUTE_NAME = GatewayRequestIdWebFilter.class.getName() + ".requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        ServerWebExchange identifiedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(HEADER_NAME, requestId)))
                .build();
        identifiedExchange.getAttributes().put(ATTRIBUTE_NAME, requestId);
        identifiedExchange.getResponse().getHeaders().set(HEADER_NAME, requestId);
        return chain.filter(identifiedExchange);
    }

    /**
     * 返回当前网关交换中的请求关联标识。
     *
     * @param exchange 当前交换
     * @return 关联标识；交换尚未经过本过滤器时返回 {@code null}
     */
    public static String requestId(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ATTRIBUTE_NAME);
        return value instanceof String requestId ? requestId : null;
    }
}
