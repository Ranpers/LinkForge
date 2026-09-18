package io.github.ranpers.linkforge.iam.shortdomain.application;

/**
 * 待创建的域名主机名已被占用。
 *
 * <p>主机名在平台内全局唯一且不会被释放：已停用的域名仍占用其主机名，
 * 因为存量短链仍然指向该主机名，复用会让旧短链解析到另一个租户的域名。</p>
 */
public class ShortDomainHostAlreadyExistsException extends RuntimeException {

    public ShortDomainHostAlreadyExistsException() {
        super("域名已存在");
    }
}
