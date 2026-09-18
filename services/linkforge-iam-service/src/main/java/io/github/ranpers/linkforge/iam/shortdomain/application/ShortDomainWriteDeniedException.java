package io.github.ranpers.linkforge.iam.shortdomain.application;

/** 操作者不具备所需的域名管理权限。 */
public class ShortDomainWriteDeniedException extends RuntimeException {

    public ShortDomainWriteDeniedException(String requiredPermission) {
        super("缺少 " + requiredPermission + " 权限");
    }
}
