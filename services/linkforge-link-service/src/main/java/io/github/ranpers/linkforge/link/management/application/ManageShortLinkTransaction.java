package io.github.ranpers.linkforge.link.management.application;

import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementCache;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementRepository;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 将短链管理读写限制在不包含远程授权调用的短事务内。
 *
 * @implNote 调用者使用读取结果完成 IAM 授权后再调用写方法。当前授权所依赖的域名和
 * 创建者在短链生命周期内不可变；删除等并发变化由写语句的受影响行数再次确认。
 */
@Service
public class ManageShortLinkTransaction {

    private final LinkManagementRepository repository;
    private final LinkManagementCache cache;

    public ManageShortLinkTransaction(
            LinkManagementRepository repository,
            LinkManagementCache cache
    ) {
        this.repository = repository;
        this.cache = cache;
    }

    /**
     * 读取 IAM 决策所需的本地短链上下文。
     *
     * @param linkId 非空的短链标识
     * @return 未删除的短链上下文；不存在时返回 {@code null}
     */
    @Transactional(readOnly = true)
    public LinkManagementSnapshot find(UUID linkId) {
        return repository.find(linkId);
    }

    /**
     * 更新目标地址并安排提交后的缓存刷新。
     *
     * @param linkId 已完成 IAM 授权的短链标识
     * @param fullUrl 已通过领域校验的绝对 HTTP(S) 地址
     * @return 更新成功时返回 {@code true}；短链已不存在时返回 {@code false}
     */
    @Transactional
    public boolean updateTarget(UUID linkId, String fullUrl) {
        boolean updated = repository.updateTarget(linkId, fullUrl);
        if (updated) {
            cache.refreshAfterCommit(linkId);
        }
        return updated;
    }

    /**
     * 修改可用状态并在有效结果提交后刷新缓存。
     *
     * @param linkId 已完成 IAM 授权的短链标识
     * @param enabled {@code true} 表示恢复手动停用的短链，{@code false} 表示手动停用
     * @return 明确区分已修改、幂等重放、禁止转换和不存在的结果
     */
    @Transactional
    public LinkManagementRepository.AvailabilityChangeResult changeAvailability(
            UUID linkId,
            boolean enabled
    ) {
        LinkManagementRepository.AvailabilityChangeResult result =
                repository.changeAvailability(linkId, enabled);
        if (result == LinkManagementRepository.AvailabilityChangeResult.CHANGED
                || result == LinkManagementRepository.AvailabilityChangeResult.UNCHANGED) {
            cache.refreshAfterCommit(linkId);
        }
        return result;
    }

    /**
     * 软删除短链并安排提交后的缓存刷新。
     *
     * @param linkId 已完成 IAM 授权的短链标识
     * @return 删除成功时返回 {@code true}；短链已不存在时返回 {@code false}
     */
    @Transactional
    public boolean softDelete(UUID linkId) {
        boolean deleted = repository.softDelete(linkId);
        if (deleted) {
            cache.refreshAfterCommit(linkId);
        }
        return deleted;
    }
}
