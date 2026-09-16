package io.github.ranpers.linkforge.link.creation.application;

public class ShortCodeAlreadyExistsException extends RuntimeException {
    public ShortCodeAlreadyExistsException() {
        super("同一域名下的短码已经存在");
    }
}
