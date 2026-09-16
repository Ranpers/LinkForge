package io.github.ranpers.linkforge.iam.domain.application.port.in;

/** 修改域名的入站端口。 */
public interface UpdateDomainUseCase {

    /**
     * 修改域名的展示名称。展示名称不参与解析，因此不产生控制事件。
     *
     * @param command 操作者、目标域名与新展示名称
     * @throws io.github.ranpers.linkforge.iam.domain.application.DomainNotFoundException 域名不存在
     * @throws io.github.ranpers.linkforge.iam.domain.application.DomainWriteDeniedException 操作者不具备 {@code domain:update} 权限，或账号非正常状态
     */
    void update(UpdateDomainCommand command);
}
