package io.github.ranpers.linkforge.iam.shortdomain.application;

public class ShortDomainNotFoundException extends RuntimeException {
    public ShortDomainNotFoundException() {
        super("域名不存在");
    }
}
