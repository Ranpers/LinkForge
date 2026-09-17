package io.github.ranpers.linkforge.iam.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Locale;

/**
 * 创建具有稳定业务代码和请求关联标识的 RFC 9457 问题响应。
 */
public final class ApiProblems {

    private static final String TYPE_PREFIX = "https://linkforge.dev/problems/";

    private ApiProblems() {
    }

    /**
     * 创建使用当前请求关联标识的问题响应。
     */
    public static ProblemDetail create(HttpStatus status, String code, String detail) {
        return create(status, code, detail, RequestTraceContext.currentTraceId());
    }

    /**
     * 创建使用指定关联标识的问题响应。
     */
    public static ProblemDetail create(
            HttpStatus status,
            String code,
            String detail,
            String traceId
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create(TYPE_PREFIX + code.toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setProperty("code", code);
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        return problem;
    }

    /**
     * 创建包含授权决策信息的问题响应。
     */
    public static ProblemDetail authorization(
            HttpStatus status,
            String code,
            String detail,
            String reasonCode,
            Object decisionId
    ) {
        ProblemDetail problem = create(status, code, detail);
        problem.setProperty("reasonCode", reasonCode);
        problem.setProperty("decisionId", decisionId);
        return problem;
    }
}
