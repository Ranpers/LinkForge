package io.github.ranpers.linkforge.iam.security.application;

public class SecurityDispositionDeniedException extends RuntimeException {
    public SecurityDispositionDeniedException() {
        super("当前用户无权执行链接安全处置");
    }
}
