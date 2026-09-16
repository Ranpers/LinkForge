package io.github.ranpers.linkforge.iam.user.domain;

/**
 * 用户名唯一是用户域的领域规则:预查与并发落库兜底(唯一约束)均抛出本异常,
 * 对外错误码的翻译由入站 web 适配器负责
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(Username username) {
        super("用户名已存在: " + username.value());
    }
}
