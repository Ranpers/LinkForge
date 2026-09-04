package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * 从 Link 服务本地数据库读取解析所需的权威运行时投影。
 */
public interface LinkRuntimeFactSource {

    /**
     * @param host 已规范化为小写的请求域名
     * @param linkCode 域名内的短码
     * @return 按域名和短码找到的短链事实，不存在时为空
     */
    Optional<LinkRuntimeFacts> findLink(String host, String linkCode);

    /**
     * @param linkId 短链唯一标识
     * @return 按唯一标识找到的短链事实，不存在时为空
     */
    Optional<LinkRuntimeFacts> findLink(UUID linkId);

    /**
     * @param domainId 域名唯一标识
     * @return 域名运行时状态，不存在时为空
     */
    Optional<DomainRuntimeState> findDomain(UUID domainId);

    /**
     * 返回用户当前全部活动限制。
     *
     * @param userId 短链创建者
     * @return 不可为空的完整限制集合；没有限制时包含空列表
     */
    UserSecurityRestrictionSet findRestrictions(UUID userId);
}
