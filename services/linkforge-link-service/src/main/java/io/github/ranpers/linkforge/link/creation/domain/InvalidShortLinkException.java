package io.github.ranpers.linkforge.link.creation.domain;

public class InvalidShortLinkException extends RuntimeException {

    public InvalidShortLinkException(String message) {
        super(message);
    }

    public InvalidShortLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
