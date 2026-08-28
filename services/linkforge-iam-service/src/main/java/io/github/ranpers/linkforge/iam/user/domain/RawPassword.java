package io.github.ranpers.linkforge.iam.user.domain;

import java.nio.charset.StandardCharsets;

/** BCrypt 当前输入策略：6-64 个 Java 字符，且 UTF-8 编码不超过 72 字节。 */
public record RawPassword(String value) {

    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 64;
    private static final int BCRYPT_MAX_BYTES = 72;

    public RawPassword {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException("密码不能为空");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidUserDataException("密码长度需在 6-64 位之间");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidUserDataException("密码的 UTF-8 编码不能超过 72 字节");
        }
    }

    public static RawPassword of(String value) {
        return new RawPassword(value);
    }
}
