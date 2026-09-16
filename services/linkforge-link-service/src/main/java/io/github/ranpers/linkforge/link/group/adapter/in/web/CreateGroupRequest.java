package io.github.ranpers.linkforge.link.group.adapter.in.web;

import io.github.ranpers.linkforge.link.group.domain.GroupName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分组的请求体。
 *
 * @param name 分组名称，去除首尾空白后需为 1 到 {@value GroupName#MAX_LENGTH} 个字符
 * @param sortOrder 展示排序提示，须为非负数；为 {@code null} 时按 0 处理
 */
public record CreateGroupRequest(
        @NotBlank @Size(max = GroupName.MAX_LENGTH) String name,
        @Min(0) Integer sortOrder
) {
}
