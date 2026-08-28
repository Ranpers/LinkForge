package io.github.ranpers.linkforge.iam.user.adapter.in.web;

/**
 * IAM HTTP API 响应模型，仅属于入站 Web 适配器。
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), data);
    }

    static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.code(), message, null);
    }
}
