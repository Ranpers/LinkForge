package io.github.ranpers.linkforge.link.group.domain;

/**
 * 分组名称。
 *
 * <p>构造时去除首尾空白，归一化后的长度必须落在 1 到 {@value #MAX_LENGTH} 之间。
 * 相等性基于归一化后的值，因此仅首尾空白不同的两个名称视为同名。</p>
 *
 * @param value 归一化后的名称，保证非空且不含首尾空白
 */
public record GroupName(String value) {

    /** 与 {@code t_group.name} 的列宽一致。 */
    public static final int MAX_LENGTH = 64;

    public GroupName {
        if (value == null || value.isBlank()) {
            throw new InvalidGroupNameException("分组名称不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidGroupNameException("分组名称长度不能超过 " + MAX_LENGTH);
        }
        value = normalized;
    }
}
