package io.github.ranpers.linkforge.iam.user.adapter.in.web;

/**
 * IAM Web 适配器错误码及对应的 HTTP 状态码。
 */
enum ErrorCode {

    SUCCESS(0, "成功", 200),
    USERNAME_ALREADY_EXISTS(20001, "用户名已存在", 409),
    PARAM_INVALID(20002, "请求参数不合法", 400),
    SYSTEM_ERROR(50000, "系统繁忙,请稍后重试", 500),
    FORBIDDEN(50001, "无权限访问", 403);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    int code() {
        return code;
    }

    String message() {
        return message;
    }

    int httpStatus() {
        return httpStatus;
    }
}
