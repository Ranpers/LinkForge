package io.github.ranpers.linkforge.iam.control.domain;

/**
 * 表示调用方提供的控制事件追踪标识不满足公开契约。
 */
public class InvalidControlEventTraceIdException extends IllegalArgumentException {

    public InvalidControlEventTraceIdException(String message) {
        super(message);
    }
}
