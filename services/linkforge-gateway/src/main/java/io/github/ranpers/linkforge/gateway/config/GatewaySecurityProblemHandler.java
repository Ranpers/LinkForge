package io.github.ranpers.linkforge.gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 将网关安全过滤器产生的 401 和 403 转换为统一问题响应。
 */
@Component
public final class GatewaySecurityProblemHandler
        implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final GatewayProblemWriter problemWriter;

    public GatewaySecurityProblemHandler(GatewayProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException exception
    ) {
        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return problemWriter.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_REQUIRED",
                "需要有效的访问令牌"
        );
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            org.springframework.security.access.AccessDeniedException exception
    ) {
        return problemWriter.write(
                exchange,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "当前主体无权执行该操作"
        );
    }
}
