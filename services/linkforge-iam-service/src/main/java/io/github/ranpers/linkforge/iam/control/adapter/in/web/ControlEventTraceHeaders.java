package io.github.ranpers.linkforge.iam.control.adapter.in.web;

import io.github.ranpers.linkforge.iam.infrastructure.web.RequestTraceContext;

/**
 * 定义 IAM 控制事件入口使用的稳定 HTTP 头名称。
 */
public final class ControlEventTraceHeaders {

    public static final String TRACE_ID = RequestTraceContext.HEADER_NAME;

    private ControlEventTraceHeaders() {
    }
}
