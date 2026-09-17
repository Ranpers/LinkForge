package io.github.ranpers.linkforge.iam.control.adapter.in.web;

import io.github.ranpers.linkforge.webmvc.request.RequestIdContext;

/**
 * 定义 IAM 控制事件入口使用的稳定 HTTP 头名称。
 */
public final class ControlEventRequestHeaders {

    public static final String REQUEST_ID = RequestIdContext.HEADER_NAME;

    private ControlEventRequestHeaders() {
    }
}
