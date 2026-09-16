package io.github.ranpers.linkforge.link.creation.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 保证公开短码满足其分配方式对应的格式，并保持原始大小写。
 *
 * @param value 域名内使用的非空路径片段；不会执行大小写或空白归一化
 * @param type  短码的分配方式
 */
public record ShortCode(String value, ShortCodeType type) {

    private static final Pattern GENERATED = Pattern.compile("[0-9A-Za-z]{10}");
    private static final Pattern CUSTOM = Pattern.compile("[A-Za-z0-9_-]{4,32}");
    private static final Set<String> RESERVED = Set.of(
            "actuator",
            "api",
            "error",
            "health",
            "login",
            "oauth2",
            "swagger-ui",
            "v3"
    );

    public ShortCode {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(type, "type");
        switch (type) {
            case GENERATED -> requireGenerated(value);
            case CUSTOM -> requireCustom(value);
        }
    }

    /**
     * 建立系统生成短码。
     *
     * @param value 恰好 10 位且仅包含 Base62 字符的值
     * @return 保持输入大小写的系统生成短码
     * @throws InvalidShortLinkException 输入不符合系统短码格式时
     */
    public static ShortCode generated(String value) {
        return new ShortCode(value, ShortCodeType.GENERATED);
    }

    /**
     * 建立调用者指定短码。
     *
     * @param value 4 至 32 位且仅包含 ASCII 字母、数字、下划线或连字符的值
     * @return 保持输入大小写的自定义短码
     * @throws InvalidShortLinkException 输入格式非法或命中保留词时
     */
    public static ShortCode custom(String value) {
        return new ShortCode(value, ShortCodeType.CUSTOM);
    }

    private static void requireGenerated(String value) {
        if (!GENERATED.matcher(value).matches()) {
            throw new InvalidShortLinkException("系统短码必须是 10 位 Base62 字符");
        }
    }

    private static void requireCustom(String value) {
        if (!CUSTOM.matcher(value).matches()) {
            throw new InvalidShortLinkException(
                    "自定义短码只能包含字母、数字、下划线和连字符，长度为 4~32"
            );
        }
        if (RESERVED.contains(value.toLowerCase(Locale.ROOT))) {
            throw new InvalidShortLinkException("自定义短码属于系统保留词");
        }
    }
}
