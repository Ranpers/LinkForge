package io.github.ranpers.linkforge.iam.domain.application;

public class DomainAvailabilityChangeDeniedException extends RuntimeException {
    public DomainAvailabilityChangeDeniedException() {
        super("当前用户没有修改域名状态的权限");
    }
}
