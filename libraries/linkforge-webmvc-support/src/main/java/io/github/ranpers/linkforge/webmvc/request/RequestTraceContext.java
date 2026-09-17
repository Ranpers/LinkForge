package io.github.ranpers.linkforge.webmvc.request;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 保存当前 HTTP 请求的服务端关联标识。
 */
public final class RequestTraceContext {

    public static final String HEADER_NAME = "X-Trace-Id";
    public static final String ATTRIBUTE_NAME = RequestTraceContext.class.getName() + ".traceId";

    private RequestTraceContext() {
    }

    /**
     * 返回当前请求的关联标识。
     *
     * @return 当前请求关联标识；不在 HTTP 请求线程中时返回 {@code null}
     */
    public static String currentTraceId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object value = attributes.getAttribute(ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST);
        return value instanceof String traceId ? traceId : null;
    }
}
