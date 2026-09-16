package io.github.ranpers.linkforge.link.management.domain;

public class InvalidManagedTargetUrlException extends IllegalArgumentException {
    public InvalidManagedTargetUrlException(String message) {
        super(message);
    }

    public InvalidManagedTargetUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
