package io.github.ranpers.linkforge.iam.domain.application;

/** 操作者不具备所需的域名管理权限。 */
public class DomainWriteDeniedException extends RuntimeException {

    public DomainWriteDeniedException(String requiredPermission) {
        super("缺少 " + requiredPermission + " 权限");
    }
}
