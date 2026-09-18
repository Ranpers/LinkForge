package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * 缓存短链解析所需的独立运行时事实。
 *
 * <p>缓存未命中不代表业务对象不存在；调用方必须回源持久化投影。事务变更期间的键
 * 由写屏障强制表现为未命中，普通回源写入也不得覆盖该屏障。</p>
 */
public interface LinkRuntimeCache {

    /**
     * 读取没有处于变更屏障中的短链事实。
     *
     * @param host 已规范化为小写的请求域名
     * @param linkCode 域名内的短码
     * @return 命中的短链事实，未命中时为空
     */
    Optional<LinkRuntimeFacts> findLink(String host, String linkCode);

    /**
     * 读取没有处于变更屏障中的域名状态。
     *
     * @param shortDomainId 域名唯一标识
     * @return 命中的域名状态，未命中时为空
     */
    Optional<ShortDomainRuntimeState> findShortDomain(UUID shortDomainId);

    /**
     * 读取没有处于变更屏障中的用户安全限制。
     *
     * @param userId 短链创建者
     * @return 命中的用户安全限制集合，未命中时为空
     */
    Optional<UserSecurityRestrictionSet> findRestrictions(UUID userId);

    /**
     * 缓存短链事实；对应键存在写屏障时忽略本次普通回源写入。
     *
     * @param link 要按其域名和短码缓存的短链事实
     */
    void putLink(LinkRuntimeFacts link);

    /**
     * 缓存域名状态；对应键存在写屏障时忽略本次普通回源写入。
     *
     * @param shortDomain 要按域名标识缓存的运行时状态
     */
    void putShortDomain(ShortDomainRuntimeState shortDomain);

    /**
     * 缓存用户安全限制；对应键存在写屏障时忽略本次普通回源写入。
     *
     * @param restrictions 要按用户标识缓存的完整活动限制集合
     */
    void putRestrictions(UserSecurityRestrictionSet restrictions);

    /**
     * 原子地建立短链写屏障并删除当前缓存值。
     *
     * @param host     已规范化为小写的域名
     * @param linkCode 域名内的短码
     * @return 当前事务用于完成或撤销屏障的唯一令牌
     * @throws RuntimeCacheMutationException Redis 无法确认屏障已经建立时
     */
    String beginLinkMutation(String host, String linkCode);

    /**
     * 原子地写入已提交的短链事实并释放写屏障。
     *
     * @param link  已提交的最新短链事实
     * @param token 建立屏障时返回的唯一令牌
     */
    void completeLinkMutation(LinkRuntimeFacts link, String token);

    /**
     * 释放已回滚短链事务留下的写屏障。
     *
     * @param host     已规范化为小写的域名
     * @param linkCode 域名内的短码
     * @param token    建立屏障时返回的唯一令牌
     */
    void cancelLinkMutation(String host, String linkCode, String token);

    /**
     * 原子地建立域名控制状态写屏障并删除当前缓存值。
     *
     * @param shortDomainId 域名唯一标识
     * @return 当前事务用于完成或撤销屏障的唯一令牌
     * @throws RuntimeCacheMutationException Redis 无法确认屏障已经建立时
     */
    String beginShortDomainMutation(UUID shortDomainId);

    /**
     * 原子地写入已提交的域名状态并释放写屏障。
     *
     * @param shortDomain 已提交的最新域名状态
     * @param token  建立屏障时返回的唯一令牌
     */
    void completeShortDomainMutation(ShortDomainRuntimeState shortDomain, String token);

    /**
     * 释放已回滚域名控制事务留下的写屏障。
     *
     * @param shortDomainId 已回滚控制事件对应的域名标识
     * @param token    建立屏障时返回的唯一令牌
     */
    void cancelShortDomainMutation(UUID shortDomainId, String token);

    /**
     * 原子地建立用户安全限制写屏障并删除当前缓存值。
     *
     * @param userId 用户唯一标识
     * @return 当前事务用于完成或撤销屏障的唯一令牌
     * @throws RuntimeCacheMutationException Redis 无法确认屏障已经建立时
     */
    String beginRestrictionsMutation(UUID userId);

    /**
     * 原子地写入已提交的用户安全限制并释放写屏障。
     *
     * @param restrictions 已提交的完整安全限制集合
     * @param token        建立屏障时返回的唯一令牌
     */
    void completeRestrictionsMutation(UserSecurityRestrictionSet restrictions, String token);

    /**
     * 释放已回滚用户安全控制事务留下的写屏障。
     *
     * @param userId 已回滚控制事件对应的用户标识
     * @param token  建立屏障时返回的唯一令牌
     */
    void cancelRestrictionsMutation(UUID userId, String token);
}
