package io.github.ranpers.linkforge.iam.user.application.port.in;

/**
 * 为具备用户管理权限的操作者提供分页用户查询。
 */
public interface ListUsersUseCase {

    /**
     * 按稳定游标返回不含凭据的用户管理投影。
     *
     * @param query 状态过滤、游标和页大小
     * @return 至多为请求页大小的结果及后续页标识；无数据时返回空列表
     */
    UserPage list(ListUsersQuery query);
}
