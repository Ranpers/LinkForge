package io.github.ranpers.linkforge.link.control.adapter.in.messaging;

public class InvalidLinkControlEventException extends RuntimeException {
    public InvalidLinkControlEventException(String message) {
        super(message);
    }

    public InvalidLinkControlEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
