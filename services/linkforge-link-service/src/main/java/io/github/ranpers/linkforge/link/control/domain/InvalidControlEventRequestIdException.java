package io.github.ranpers.linkforge.link.control.domain;

/**
 * 表示控制事件携带的关联标识违反跨进程契约。
 */
public class InvalidControlEventRequestIdException extends IllegalArgumentException {

    public InvalidControlEventRequestIdException(String message) {
        super(message);
    }
}
