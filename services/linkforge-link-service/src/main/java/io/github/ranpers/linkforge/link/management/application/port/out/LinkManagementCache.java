package io.github.ranpers.linkforge.link.management.application.port.out;

import java.util.UUID;

/**
 * 通过写屏障使短链管理事务与运行时缓存保持一致。
 */
public interface LinkManagementCache {

    /**
     * 立即建立缓存写屏障，并注册事务完成后的缓存刷新或屏障撤销。
     *
     * @param linkId 已发生管理写入的短链
     * @throws io.github.ranpers.linkforge.link.resolution.application.port.out.RuntimeCacheMutationException
     *         无法确认写屏障已经建立时
     */
    void synchronizeMutation(UUID linkId);
}
