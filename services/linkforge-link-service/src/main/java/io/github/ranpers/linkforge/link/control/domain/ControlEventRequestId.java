package io.github.ranpers.linkforge.link.control.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * 保证收到的控制事件关联标识与 HTTP 响应头、日志使用同一个规范 UUID。
 *
 * @param value 不可为 {@code null} 的规范 UUID
 * @apiNote 大小写与 {@link UUID} 一致地不敏感，但对外一律使用小写规范形式。简写形式
 * （如 {@code 1-1-1-1-1}）虽然能被 {@link UUID#fromString(String)} 解析，仍会被拒绝：
 * 允许它会让同一个标识出现两种文本形态，破坏跨服务按字符串比对的可靠性。
 */
public record ControlEventRequestId(UUID value) {

    public ControlEventRequestId {
        Objects.requireNonNull(value, "value");
    }

    /**
     * 将可缺失的消息字段转换为受校验的关联标识。
     *
     * @param value 可为空的原始值；非空时必须是规范 UUID 文本
     * @return 输入为空时返回 {@code null}；否则返回规范化后的关联标识
     * @throws InvalidControlEventRequestIdException 输入不是规范 UUID 时
     */
    public static ControlEventRequestId fromNullable(String value) {
        if (value == null) {
            return null;
        }
        UUID parsed;
        try {
            parsed = UUID.fromString(value);
        }
        catch (IllegalArgumentException exception) {
            throw new InvalidControlEventRequestIdException("requestId 必须是规范 UUID");
        }
        if (!parsed.toString().equalsIgnoreCase(value)) {
            throw new InvalidControlEventRequestIdException("requestId 必须是规范 UUID");
        }
        return new ControlEventRequestId(parsed);
    }
}
