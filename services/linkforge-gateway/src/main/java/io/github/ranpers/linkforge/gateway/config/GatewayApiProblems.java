package io.github.ranpers.linkforge.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Locale;

/**
 * 创建与后端服务一致的 RFC 9457 问题响应。
 *
 * @implNote 网关运行在 WebFlux 上，不能依赖只服务 Servlet 应用的 {@code linkforge-webmvc-support}
 * 模块，因此保留一份等价实现。类型 URI 前缀与业务码转写规则必须与后端的 {@code ApiProblems} 保持一致，
 * 否则同一个业务码会在两条路径上得到不同的 {@code type}。
 */
final class GatewayApiProblems {

    private static final String TYPE_PREFIX = "https://linkforge.dev/problems/";

    private GatewayApiProblems() {
    }

    /**
     * 创建问题响应。
     *
     * @param status HTTP 状态码
     * @param code 稳定的业务错误码
     * @param detail 面向调用方的说明
     * @param requestId 当前请求关联标识；为 {@code null} 时省略该属性
     * @return 可直接序列化的问题响应
     */
    static ProblemDetail create(HttpStatus status, String code, String detail, String requestId) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create(TYPE_PREFIX + code.toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setProperty("code", code);
        if (requestId != null) {
            problem.setProperty("requestId", requestId);
        }
        return problem;
    }
}
