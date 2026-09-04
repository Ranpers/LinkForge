package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * 缓存短链解析所需的独立运行时事实。
 *
 * <p>缓存未命中不代表业务对象不存在；调用方必须回源持久化投影。</p>
 */
public interface LinkRuntimeCache {

    /**
     * @param host 已规范化为小写的请求域名
     * @param linkCode 域名内的短码
     * @return 命中的短链事实，未命中时为空
     */
    Optional<LinkRuntimeFacts> findLink(String host, String linkCode);

    /**
     * @param domainId 域名唯一标识
     * @return 命中的域名状态，未命中时为空
     */
    Optional<DomainRuntimeState> findDomain(UUID domainId);

    /**
     * @param userId 短链创建者
     * @return 命中的用户安全限制集合，未命中时为空
     */
    Optional<UserSecurityRestrictionSet> findRestrictions(UUID userId);

    /** @param link 要按其域名和短码缓存的短链事实 */
    void putLink(LinkRuntimeFacts link);

    /** @param domain 要按域名标识缓存的运行时状态 */
    void putDomain(DomainRuntimeState domain);

    /** @param restrictions 要按用户标识缓存的完整活动限制集合 */
    void putRestrictions(UserSecurityRestrictionSet restrictions);
}
