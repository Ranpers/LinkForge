package io.github.ranpers.linkforge.link.management.application;

public class ShortLinkNotFoundException extends RuntimeException {
    public ShortLinkNotFoundException() {
        super("短链不存在");
    }
}
