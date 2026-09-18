package io.github.ranpers.linkforge.iam.shortdomain.application.port.in;

import java.util.UUID;

/**
 * 修改域名的入参。
 *
 * <p>只允许修改展示名称。主机名是存量短链的解析依据，改名会让已发布的短链全部失效，
 * 因此不作为可修改字段；需要新主机名时应新建域名。</p>
 *
 * @param actorUserId 发起操作的已认证用户
 * @param shortDomainId    目标域名标识
 * @param name        新的展示名称；为 {@code null} 表示清空该字段
 */
public record UpdateShortDomainCommand(UUID actorUserId, UUID shortDomainId, String name) {
}
