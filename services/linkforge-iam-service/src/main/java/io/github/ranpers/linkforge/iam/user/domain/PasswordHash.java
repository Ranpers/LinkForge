package io.github.ranpers.linkforge.iam.user.domain;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException("密码散列不能为空");
        }
    }
}
