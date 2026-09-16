package io.github.ranpers.linkforge.link.group.application.port.out;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;

import java.util.List;

/** 分组列表只读持久化端口。 */
public interface GroupListQuery {

    /**
     * 按游标读取一页分组，返回顺序为创建时间倒序、标识倒序。
     *
     * @param criteria 归属用户、游标与抓取行数
     * @return 至多 {@code fetchSize} 条数据；无匹配时返回空列表
     */
    List<GroupView> find(GroupListCriteria criteria);
}
