package io.github.ranpers.linkforge.link.creation.application;

public class IamAuthorizationUnavailableException extends RuntimeException {
    public IamAuthorizationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public IamAuthorizationUnavailableException(String message) {
        super(message);
    }
}
