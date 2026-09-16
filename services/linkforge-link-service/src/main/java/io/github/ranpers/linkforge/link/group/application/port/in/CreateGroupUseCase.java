package io.github.ranpers.linkforge.link.group.application.port.in;

/**
 * 创建分组的入站端口。
 *
 * <p>名称在同一归属用户的活跃分组内必须唯一；与已软删除的分组重名不受限制。</p>
 */
public interface CreateGroupUseCase {

    /**
     * 创建归属于指定用户的分组，归属关系由调用方从访问令牌推导，不可由请求体覆盖。
     *
     * @param command 归属用户、原始名称与排序提示
     * @return 新建分组的只读投影，含服务端生成的标识与 UTC 时间戳
     * @throws io.github.ranpers.linkforge.link.group.domain.InvalidGroupNameException 名称去除首尾空白后为空，或长度超过 64
     * @throws io.github.ranpers.linkforge.link.group.application.GroupAlreadyExistsException 该用户的活跃分组中已存在同名分组
     */
    GroupView create(CreateGroupCommand command);
}
