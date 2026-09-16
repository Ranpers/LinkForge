package io.github.ranpers.linkforge.iam.domain.application.port.in;

/**
 * 分页查询当前操作者有权读取的域名。
 */
public interface ListDomainsUseCase {

    /**
     * 按稳定游标返回域名管理投影。
     *
     * @param query 操作者、状态过滤、游标和页大小
     * @return 至多为请求页大小的结果及后续页标识；无数据时返回空列表
     */
    DomainPage list(ListDomainsQuery query);
}
