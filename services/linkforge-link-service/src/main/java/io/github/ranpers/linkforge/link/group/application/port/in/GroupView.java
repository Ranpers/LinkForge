package io.github.ranpers.linkforge.link.group.application.port.in;

import java.time.Instant;
import java.util.UUID;

/**
 * 分组的只读投影，同时用于列表与详情。
 *
 * <p>不含归属用户标识：所有读取都以访问令牌主体为过滤条件，调用方无需也无法读到他人分组。</p>
 *
 * @param id 分组标识
 * @param name 归一化后的分组名称
 * @param sortOrder 展示排序提示，由前端使用；服务端按创建时间倒序返回
 * @param createdAt 创建时间，UTC
 * @param updatedAt 最近一次修改时间，UTC
 */
public record GroupView(UUID id, String name, int sortOrder, Instant createdAt, Instant updatedAt) {
}
