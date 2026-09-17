package io.github.ranpers.linkforge.webmvc.request;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 保存当前 HTTP 请求的业务关联标识。
 *
 * @apiNote 该标识只用于把同一次调用产生的日志、问题响应和控制事件串联起来，不参与认证与授权判定，
 * 也不替代 OpenTelemetry 的 W3C {@code traceparent}；两者可以同时存在且取值互不相关。
 */
public final class RequestIdContext {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String ATTRIBUTE_NAME = RequestIdContext.class.getName() + ".requestId";
    public static final String MDC_KEY = "requestId";

    private RequestIdContext() {
    }

    /**
     * 返回当前请求的关联标识。
     *
     * @return 当前请求关联标识；不在 HTTP 请求线程中时返回 {@code null}
     */
    public static String currentRequestId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object value = attributes.getAttribute(ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST);
        return value instanceof String requestId ? requestId : null;
    }
}
