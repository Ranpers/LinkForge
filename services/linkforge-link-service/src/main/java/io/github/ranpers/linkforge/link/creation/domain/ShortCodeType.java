package io.github.ranpers.linkforge.link.creation.domain;

/**
 * 标识公开短码是由系统分配还是由调用者指定。
 */
public enum ShortCodeType {
    /**
     * 系统通过安全随机数生成的短码。
     */
    GENERATED,

    /**
     * 调用者明确指定的短码。
     */
    CUSTOM
}
