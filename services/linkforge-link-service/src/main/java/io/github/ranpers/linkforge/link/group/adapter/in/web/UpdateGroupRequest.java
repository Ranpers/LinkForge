package io.github.ranpers.linkforge.link.group.adapter.in.web;

import io.github.ranpers.linkforge.link.group.domain.GroupName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改分组的请求体。
 *
 * @param name 新的分组名称，必填；仅调整排序时回传当前名称
 * @param sortOrder 新的展示排序提示，须为非负数；为 {@code null} 时保留原值
 */
public record UpdateGroupRequest(
        @NotBlank @Size(max = GroupName.MAX_LENGTH) String name,
        @Min(0) Integer sortOrder
) {
}
