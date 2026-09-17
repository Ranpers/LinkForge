package io.github.ranpers.linkforge.link.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 将资源服务器过滤器链产生的 401 和 403 转换为统一问题响应。
 */
@Component
public final class ApiSecurityProblemHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiSecurityProblemHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        write(request, response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "需要有效的访问令牌");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "当前主体无权执行该操作");
    }

    private void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String detail
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        Object value = request.getAttribute(RequestTraceContext.ATTRIBUTE_NAME);
        String traceId = value instanceof String id ? id : null;
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiProblems.create(status, code, detail, traceId)
        );
    }
}
