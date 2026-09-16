package io.github.ranpers.linkforge.link.group.domain;

/** 分组名称不满足 {@link GroupName} 的归一化与长度约束。 */
public class InvalidGroupNameException extends RuntimeException {

    public InvalidGroupNameException(String message) {
        super(message);
    }
}
