package io.github.ranpers.linkforge.iam.shortdomain.application;

public class ShortDomainAvailabilityChangeDeniedException extends RuntimeException {
    public ShortDomainAvailabilityChangeDeniedException() {
        super("当前用户没有修改域名状态的权限");
    }
}
