package io.github.ranpers.linkforge.link.control.domain;

/**
 * 表示控制事件携带的追踪标识违反跨进程契约。
 */
public class InvalidControlEventTraceIdException extends IllegalArgumentException {

    public InvalidControlEventTraceIdException(String message) {
        super(message);
    }
}
