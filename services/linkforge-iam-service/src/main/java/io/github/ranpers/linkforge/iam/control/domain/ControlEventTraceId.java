package io.github.ranpers.linkforge.iam.control.domain;

import java.util.Objects;

/**
 * 保证控制事件关联标识可安全写入事件信封与数据库。
 *
 * @param value 1 至 64 个 Unicode 字符的非空白不透明标识；不会执行空白或大小写归一化
 */
public record ControlEventTraceId(String value) {

    public static final int MAX_LENGTH = 64;

    public ControlEventTraceId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidControlEventTraceIdException("traceId 不能为空白");
        }
        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new InvalidControlEventTraceIdException("traceId 长度不能超过 64 个字符");
        }
    }

    /**
     * 将可缺失的入站值转换为受校验的追踪标识。
     *
     * @param value 可为空的原始值；非空时必须满足本类型不变量
     * @return 输入为空时返回 {@code null}；否则返回保持原值的追踪标识
     * @throws InvalidControlEventTraceIdException 输入为空白或超过 64 个字符时
     */
    public static ControlEventTraceId fromNullable(String value) {
        return value == null
                ? null
                : new ControlEventTraceId(value);
    }
}
