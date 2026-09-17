package io.github.ranpers.linkforge.iam.domain.application.port.in;

/**
 * 创建域名的入站端口。
 *
 * <p>创建成功后域名立即可用：状态为启用，并直接授权给创建者，
 * 使其无需等待额外的授权操作就能在该域名下创建短链。</p>
 */
public interface CreateDomainUseCase {

    /**
     * 注册一个新域名，并向 link-service 发布控制事件以建立解析所需的状态投影。
     *
     * @param command 操作者、原始主机名、展示名称与可选关联标识
     * @return 新建域名的只读投影，含服务端生成的标识与 UTC 时间戳
     * @throws io.github.ranpers.linkforge.iam.domain.domain.InvalidDomainHostException 主机名去除首尾空白后为空、超过 253 字符或不满足点分主机名格式
     * @throws io.github.ranpers.linkforge.iam.domain.application.DomainHostAlreadyExistsException 该主机名已被占用
     * @throws io.github.ranpers.linkforge.iam.domain.application.DomainWriteDeniedException 操作者不具备 {@code domain:create} 权限，或账号非正常状态
     */
    DomainListItem create(CreateDomainCommand command);
}
