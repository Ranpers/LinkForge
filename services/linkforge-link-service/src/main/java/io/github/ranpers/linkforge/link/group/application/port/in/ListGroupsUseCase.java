package io.github.ranpers.linkforge.link.group.application.port.in;

/** 分页读取分组列表的入站端口。 */
public interface ListGroupsUseCase {

    /**
     * 按创建时间倒序返回当前用户自己的活跃分组。
     *
     * @param query 归属用户、游标与页大小
     * @return 至多 {@code pageSize} 条数据，以及是否还有下一页；无数据时返回空列表而非 null
     */
    GroupPage list(ListGroupsQuery query);
}
