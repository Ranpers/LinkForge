package io.github.ranpers.linkforge.iam.security.application;

public class SecurityTargetUserNotFoundException extends RuntimeException {
    public SecurityTargetUserNotFoundException() {
        super("安全处置目标用户不存在");
    }
}
