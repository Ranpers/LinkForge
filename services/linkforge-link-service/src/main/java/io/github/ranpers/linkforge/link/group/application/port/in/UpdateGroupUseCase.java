package io.github.ranpers.linkforge.link.group.application.port.in;

/**
 * 修改分组的入站端口。
 *
 * <p>名称与排序条件同时修改；名称在同一归属用户的活跃分组内必须唯一。
 * 目标分组不属于当前操作者时按「不存在」处理，不额外区分状态码。</p>
 */
public interface UpdateGroupUseCase {

    /**
     * 重命名并可选地调整排序提示。
     *
     * @param command 归属用户、目标分组、新名称与可选排序提示
     * @throws io.github.ranpers.linkforge.link.group.domain.InvalidGroupNameException 名称去除首尾空白后为空，或长度超过 64
     * @throws io.github.ranpers.linkforge.link.group.application.GroupNotFoundException 分组不存在、已软删除或不属于该用户
     * @throws io.github.ranpers.linkforge.link.group.application.GroupAlreadyExistsException 该用户的活跃分组中已存在同名分组
     */
    void update(UpdateGroupCommand command);
}
