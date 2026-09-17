package io.github.ranpers.linkforge.webmvc.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * 为每个请求确定业务关联标识，并写入请求作用域、响应头和日志上下文。
 *
 * @apiNote 入站 {@value RequestIdContext#HEADER_NAME} 按不可信输入处理：只有本身就是规范 UUID
 * 的取值才会被沿用，其余一律丢弃并替换为新生成的值。服务间调用依赖这条保留规则来延续同一个
 * 标识，而外部调用方无法借该头向日志和控制事件注入任意文本。
 * @implNote 由 {@code LinkForgeWebMvcSupportConfiguration} 显式注册；本类刻意不声明
 * {@code @Component}，避免组件扫描范围决定其是否生效。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(RequestIdContext.HEADER_NAME));
        request.setAttribute(RequestIdContext.ATTRIBUTE_NAME, requestId);
        response.setHeader(RequestIdContext.HEADER_NAME, requestId);
        MDC.put(RequestIdContext.MDC_KEY, requestId);
        try {
            filterChain.doFilter(new RequestIdHeaderRequest(request, requestId), response);
        } finally {
            // 容器线程会被复用，不清理会让下一个请求的日志带上上一个请求的标识。
            MDC.remove(RequestIdContext.MDC_KEY);
        }
    }

    private static String resolveRequestId(String candidate) {
        if (candidate != null) {
            try {
                UUID parsed = UUID.fromString(candidate);
                if (parsed.toString().equalsIgnoreCase(candidate)) {
                    return parsed.toString();
                }
            } catch (IllegalArgumentException ignored) {
                // 非 UUID 与书写不规范但可解析的取值走同一条回退路径。
            }
        }
        return UUID.randomUUID().toString();
    }

    private static final class RequestIdHeaderRequest extends HttpServletRequestWrapper {

        private final String requestId;

        private RequestIdHeaderRequest(HttpServletRequest request, String requestId) {
            super(request);
            this.requestId = requestId;
        }

        @Override
        public String getHeader(String name) {
            return RequestIdContext.HEADER_NAME.equalsIgnoreCase(name)
                    ? requestId
                    : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return RequestIdContext.HEADER_NAME.equalsIgnoreCase(name)
                    ? Collections.enumeration(Collections.singleton(requestId))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            LinkedHashSet<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            if (original != null) {
                original.asIterator().forEachRemaining(name -> {
                    if (!RequestIdContext.HEADER_NAME.equalsIgnoreCase(name)) {
                        names.add(name);
                    }
                });
            }
            names.add(RequestIdContext.HEADER_NAME);
            return Collections.enumeration(names);
        }
    }
}
