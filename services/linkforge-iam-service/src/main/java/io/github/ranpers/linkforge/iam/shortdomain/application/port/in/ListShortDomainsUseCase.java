package io.github.ranpers.linkforge.iam.shortdomain.application.port.in;

/**
 * 分页查询当前操作者有权读取的域名。
 */
public interface ListShortDomainsUseCase {

    /**
     * 按稳定游标返回域名管理投影。
     *
     * @param query 操作者、状态过滤、游标和页大小
     * @return 至多为请求页大小的结果及后续页标识；无数据时返回空列表
     */
    ShortDomainPage list(ListShortDomainsQuery query);
}
