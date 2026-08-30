package io.github.ranpers.linkforge.link.availability.adapter.in.messaging;

/** 不符合 v1 契约的消息；属于不可恢复错误，应直接进入 DLT。 */
public class InvalidAuthorizationEventException extends RuntimeException {

    public InvalidAuthorizationEventException(String message) {
        super(message);
    }

    public InvalidAuthorizationEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
