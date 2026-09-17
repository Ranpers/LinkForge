package io.github.ranpers.linkforge.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * 将网关的错误结果写成 RFC 9457 问题响应。
 */
@Component
public final class GatewayProblemWriter {

    private static final Logger log = LoggerFactory.getLogger(GatewayProblemWriter.class);

    private final ObjectMapper objectMapper;

    public GatewayProblemWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 以问题响应结束当前交换。
     *
     * @param exchange 当前交换
     * @param status HTTP 状态码
     * @param code 稳定的业务错误码
     * @param detail 面向调用方的说明
     * @return 写入完成的信号
     * @implNote 响应一旦提交就无法再追加响应体。此时只记录告警并结束交换，不重设状态码，
     * 以免把已经发往调用方的真实结果改写成一个假的错误响应。序列化先于状态码与响应体的写入，
     * 失败时只放弃响应体：若任由异常继续抛出，它会再次进入问题异常处理器并再次序列化，
     * 最终退化成不带问题响应的裸 500，反而丢掉已经确定的状态码。
     */
    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String code, String detail) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            log.warn("响应已提交，跳过问题响应: status={} code={}", status.value(), code);
            return response.setComplete();
        }
        ProblemDetail problem = GatewayApiProblems.create(
                status,
                code,
                detail,
                GatewayRequestIdWebFilter.requestId(exchange)
        );
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(problem);
        }
        catch (RuntimeException serializationFailure) {
            log.error("问题响应序列化失败: status={} code={}", status.value(), code, serializationFailure);
            response.setStatusCode(status);
            return response.setComplete();
        }
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
