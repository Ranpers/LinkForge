package io.github.ranpers.linkforge.link.creation.application.port.in;

import io.github.ranpers.linkforge.link.creation.domain.ShortLink;

import java.util.UUID;

/**
 * 短链创建成功后的稳定标识信息。
 *
 * @param id 新短链的唯一标识
 * @param linkCode 域名内唯一的短码
 * @param domainId 短链所属域名
 */
public record CreatedShortLink(UUID id, String linkCode, UUID domainId) {

    /**
     * 从已建立领域不变量的短链生成公开结果。
     *
     * @param link 已创建或幂等重放得到的短链
     * @return 对外返回所需的稳定标识信息
     */
    public static CreatedShortLink from(ShortLink link) {
        return new CreatedShortLink(link.id(), link.linkCode(), link.domainId());
    }
}
