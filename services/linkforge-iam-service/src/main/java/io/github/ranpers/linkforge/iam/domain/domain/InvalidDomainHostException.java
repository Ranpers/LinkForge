package io.github.ranpers.linkforge.iam.domain.domain;

/** 域名主机名不满足 {@link DomainHost} 的归一化与格式约束。 */
public class InvalidDomainHostException extends RuntimeException {

    public InvalidDomainHostException(String message) {
        super(message);
    }
}
