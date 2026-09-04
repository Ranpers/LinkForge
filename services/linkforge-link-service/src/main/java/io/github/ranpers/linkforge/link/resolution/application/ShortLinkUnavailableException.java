package io.github.ranpers.linkforge.link.resolution.application;

public class ShortLinkUnavailableException extends RuntimeException {
    public ShortLinkUnavailableException() {
        super("短链不存在或当前不可用");
    }
}
