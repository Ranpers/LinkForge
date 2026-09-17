package io.github.ranpers.linkforge.webmvc.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * 保留可信上游关联标识；缺失时生成新标识，并将其写入请求和响应。
 *
 * @implNote 由 {@code LinkForgeWebMvcSupportConfiguration} 显式注册；本类刻意不声明
 * {@code @Component}，避免组件扫描范围决定其是否生效。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestTraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = normalize(request.getHeader(RequestTraceContext.HEADER_NAME));
        request.setAttribute(RequestTraceContext.ATTRIBUTE_NAME, traceId);
        response.setHeader(RequestTraceContext.HEADER_NAME, traceId);
        filterChain.doFilter(new TraceHeaderRequest(request, traceId), response);
    }

    private static String normalize(String candidate) {
        if (candidate != null
                && !candidate.isBlank()
                && candidate.codePointCount(0, candidate.length()) <= 64) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }

    private static final class TraceHeaderRequest extends HttpServletRequestWrapper {

        private final String traceId;

        private TraceHeaderRequest(HttpServletRequest request, String traceId) {
            super(request);
            this.traceId = traceId;
        }

        @Override
        public String getHeader(String name) {
            return RequestTraceContext.HEADER_NAME.equalsIgnoreCase(name)
                    ? traceId
                    : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return RequestTraceContext.HEADER_NAME.equalsIgnoreCase(name)
                    ? Collections.enumeration(Collections.singleton(traceId))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            LinkedHashSet<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            if (original != null) {
                original.asIterator().forEachRemaining(name -> {
                    if (!RequestTraceContext.HEADER_NAME.equalsIgnoreCase(name)) {
                        names.add(name);
                    }
                });
            }
            names.add(RequestTraceContext.HEADER_NAME);
            return Collections.enumeration(names);
        }
    }
}
