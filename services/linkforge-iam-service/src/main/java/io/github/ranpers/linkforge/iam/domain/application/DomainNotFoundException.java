package io.github.ranpers.linkforge.iam.domain.application;

public class DomainNotFoundException extends RuntimeException {
    public DomainNotFoundException() {
        super("域名不存在");
    }
}
