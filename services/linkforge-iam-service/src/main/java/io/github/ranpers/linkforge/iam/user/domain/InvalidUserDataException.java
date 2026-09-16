package io.github.ranpers.linkforge.iam.user.domain;

/** 用户核心数据不满足领域约束。 */
public class InvalidUserDataException extends RuntimeException {

    public InvalidUserDataException(String message) {
        super(message);
    }
}
