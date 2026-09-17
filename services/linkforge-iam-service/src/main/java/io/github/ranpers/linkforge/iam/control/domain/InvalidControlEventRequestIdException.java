package io.github.ranpers.linkforge.iam.control.domain;

/**
 * 表示调用方提供的控制事件关联标识不满足公开契约。
 */
public class InvalidControlEventRequestIdException extends IllegalArgumentException {

    public InvalidControlEventRequestIdException(String message) {
        super(message);
    }
}
