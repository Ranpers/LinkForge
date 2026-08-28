package io.github.ranpers.linkforge.iam.user.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final int MAX_LENGTH = 128;
    private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public EmailAddress {
        if (value != null && (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches())) {
            throw new InvalidUserDataException("邮箱格式不正确");
        }
    }

    public static EmailAddress optional(String value) {
        if (value == null || value.isBlank()) {
            return new EmailAddress(null);
        }
        return new EmailAddress(value.trim().toLowerCase(Locale.ROOT));
    }
}
