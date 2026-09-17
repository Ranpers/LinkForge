package io.github.ranpers.linkforge.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;

/**
 * 把未由网关过滤器直接处理的异常写成统一问题响应。
 *
 * @implNote 优先级高于 Spring Boot 默认错误处理器，否则框架会先输出 {@code application/json}
 * 的通用错误体，调用方拿不到稳定的业务码。网关不解析下游响应体，因此这里只处理转发链路自身的故障。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class GatewayProblemExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayProblemExceptionHandler.class);

    private static final int MAX_CAUSE_DEPTH = 16;

    private final GatewayProblemWriter problemWriter;

    public GatewayProblemExceptionHandler(GatewayProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        Throwable failure = classify(throwable);
        if (failure instanceof TimeoutException) {
            return problemWriter.write(
                    exchange, HttpStatus.GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT", "上游服务响应超时");
        }
        if (failure instanceof NotFoundException) {
            return problemWriter.write(
                    exchange, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "上游服务暂时不可用");
        }
        if (failure instanceof ConnectException || failure instanceof UnknownHostException) {
            log.error("无法连接上游服务", failure);
            return problemWriter.write(
                    exchange, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "上游服务暂时不可用");
        }
        if (failure instanceof ResponseStatusException statusException) {
            HttpStatus status = HttpStatus.resolve(statusException.getStatusCode().value());
            return status == null
                    ? internalError(exchange, failure)
                    : problemWriter.write(exchange, status, codeOf(status), detailOf(status, statusException));
        }
        return internalError(exchange, failure);
    }

    private Mono<Void> internalError(ServerWebExchange exchange, Throwable throwable) {
        log.error("网关未处理异常: {}", exchange.getRequest().getURI(), throwable);
        return problemWriter.write(
                exchange, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "网关暂时无法处理请求");
    }

    /**
     * 在异常链中定位第一个可识别的故障。
     *
     * @param throwable 原始异常
     * @return 用于映射状态码的异常；没有可识别的节点时返回原始异常
     * @implNote 转发链路会把底层故障逐层包装，最外层往往没有诊断价值，因此需要向下查找。
     * 查找深度有上限，避免自引用的异常链导致死循环。
     */
    private static Throwable classify(Throwable throwable) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof TimeoutException
                    || current instanceof NotFoundException
                    || current instanceof ConnectException
                    || current instanceof UnknownHostException
                    || current instanceof ResponseStatusException) {
                return current;
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return throwable;
    }

    private static String codeOf(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "NOT_FOUND";
            case METHOD_NOT_ALLOWED -> "METHOD_NOT_ALLOWED";
            default -> "HTTP_" + status.value();
        };
    }

    /**
     * 返回可以安全暴露给调用方的异常说明。
     *
     * @implNote 4xx 说明由框架依据请求内容生成，可以原样回传；5xx 说明可能暴露内部状态，
     * 一律替换为固定文案，完整原因只写进服务端日志。
     */
    private static String detailOf(HttpStatus status, ResponseStatusException exception) {
        if (status.is5xxServerError()) {
            log.error("网关异常映射为 {}", status.value(), exception);
            return "网关暂时无法处理请求";
        }
        String reason = exception.getReason();
        return reason == null ? status.getReasonPhrase() : reason;
    }
}
