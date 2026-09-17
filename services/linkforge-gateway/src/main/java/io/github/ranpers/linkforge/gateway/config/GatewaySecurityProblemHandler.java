package io.github.ranpers.linkforge.gateway.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

/**
 * 将网关安全过滤器产生的 401 和 403 转换为统一问题响应。
 */
@Component
public final class GatewaySecurityProblemHandler
        implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public GatewaySecurityProblemHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException exception
    ) {
        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_REQUIRED",
                "需要有效的访问令牌"
        );
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, org.springframework.security.access.AccessDeniedException exception) {
        return write(exchange, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "当前主体无权执行该操作");
    }

    private Mono<Void> write(
            ServerWebExchange exchange,
            HttpStatus status,
            String code,
            String detail
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("https://linkforge.dev/problems/"
                + code.toLowerCase(java.util.Locale.ROOT).replace('_', '-')));
        problem.setProperty("code", code);
        String traceId = GatewayTraceWebFilter.traceId(exchange);
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(objectMapper.writeValueAsBytes(problem));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
