package io.github.ranpers.linkforge.link.creation.application;

public class ShortCodeAllocationException extends RuntimeException {
    public ShortCodeAllocationException() {
        super("系统暂时无法分配短码");
    }
}
