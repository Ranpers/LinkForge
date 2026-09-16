package io.github.ranpers.linkforge.link.creation.application;

public class InvalidLinkGroupException extends RuntimeException {
    public InvalidLinkGroupException() {
        super("分组不存在或不属于当前用户");
    }
}
