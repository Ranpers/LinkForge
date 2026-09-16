package io.github.ranpers.linkforge.link.query.application.port.in;

/**
 * 分页查询当前操作者创建的短链接。
 */
public interface ListShortLinksUseCase {

    /**
     * 按稳定游标返回符合过滤条件的短链接管理投影。
     *
     * @param query 操作者、过滤条件、游标和页大小
     * @return 至多为请求页大小的结果及后续页标识；无数据时返回空列表
     */
    ShortLinkPage list(ListShortLinksQuery query);
}
