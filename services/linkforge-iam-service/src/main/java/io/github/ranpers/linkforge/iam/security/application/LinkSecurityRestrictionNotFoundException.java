package io.github.ranpers.linkforge.iam.security.application;

public class LinkSecurityRestrictionNotFoundException extends RuntimeException {
    public LinkSecurityRestrictionNotFoundException() {
        super("链接安全限制不存在");
    }
}
