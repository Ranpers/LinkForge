package io.github.ranpers.linkforge.link.control.domain;

import java.util.Objects;

/**
 * 保证收到的控制事件关联标识可安全写入 Inbox。
 *
 * @param value 不可为 {@code null}，包含 1 至 64 个 Unicode code point，且至少包含一个非 wire 空白码点；
 *              不会执行空白或大小写归一化
 * @apiNote 空白判定使用 wire schema 固定的 Unicode {@code White_Space} 码点集合，
 * 不依赖 {@link String#isBlank()}，因此不会随 JVM 的 Unicode 数据版本变化
 */
public record ControlEventTraceId(String value) {

    public static final int MAX_LENGTH = 64;

    public ControlEventTraceId {
        Objects.requireNonNull(value, "value");
        if (value.codePoints().allMatch(ControlEventTraceId::isWireWhitespace)) {
            throw new InvalidControlEventTraceIdException("traceId 不能为空白");
        }
        if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
            throw new InvalidControlEventTraceIdException("traceId 长度不能超过 64 个 Unicode code point");
        }
    }

    /**
     * 将可缺失的消息字段转换为受校验的追踪标识。
     *
     * @param value 可为空的原始值；非空时必须满足本类型不变量
     * @return 输入为空时返回 {@code null}；否则返回保持原值的追踪标识
     * @throws InvalidControlEventTraceIdException 输入为空白或超过 64 个 Unicode code point 时
     */
    public static ControlEventTraceId fromNullable(String value) {
        return value == null
                ? null
                : new ControlEventTraceId(value);
    }

    private static boolean isWireWhitespace(int codePoint) {
        return codePoint >= 0x0009 && codePoint <= 0x000D
                || codePoint == 0x0020
                || codePoint == 0x0085
                || codePoint == 0x00A0
                || codePoint == 0x1680
                || codePoint >= 0x2000 && codePoint <= 0x200A
                || codePoint == 0x2028
                || codePoint == 0x2029
                || codePoint == 0x202F
                || codePoint == 0x205F
                || codePoint == 0x3000;
    }
}
