package io.github.ranpers.linkforge.iam.user.domain;

import java.util.regex.Pattern;

public record Username(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[a-zA-Z0-9_]{4,64}$");

    public Username {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidUserDataException("用户名需为 4-64 位字母、数字或下划线");
        }
    }

    public static Username of(String value) {
        return new Username(value == null ? null : value.trim());
    }
}
