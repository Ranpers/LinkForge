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
 * 在安全过滤器之前生成请求关联标识，并传递给下游及调用方。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class GatewayTraceWebFilter implements WebFilter {

    public static final String HEADER_NAME = "X-Trace-Id";
    public static final String ATTRIBUTE_NAME = GatewayTraceWebFilter.class.getName() + ".traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = UUID.randomUUID().toString();
        ServerWebExchange tracedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(HEADER_NAME, traceId)))
                .build();
        tracedExchange.getAttributes().put(ATTRIBUTE_NAME, traceId);
        tracedExchange.getResponse().getHeaders().set(HEADER_NAME, traceId);
        return chain.filter(tracedExchange);
    }

    /**
     * 返回当前网关交换中的服务端关联标识。
     */
    public static String traceId(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ATTRIBUTE_NAME);
        return value instanceof String traceId ? traceId : null;
    }
}
