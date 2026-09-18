package io.github.ranpers.linkforge.iam.shortdomain.domain;

/** 域名主机名不满足 {@link ShortDomainHost} 的归一化与格式约束。 */
public class InvalidShortDomainHostException extends RuntimeException {

    public InvalidShortDomainHostException(String message) {
        super(message);
    }
}
