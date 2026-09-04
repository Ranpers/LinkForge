package io.github.ranpers.linkforge.link.management.application.port.in;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.management.application.LinkManagementDeniedException;
import io.github.ranpers.linkforge.link.management.application.LinkStateConflictException;
import io.github.ranpers.linkforge.link.management.application.ShortLinkNotFoundException;
import io.github.ranpers.linkforge.link.management.domain.InvalidManagedTargetUrlException;

import java.util.UUID;

/**
 * 在 IAM 授权后修改短链的生命周期和跳转目标。
 */
public interface ManageShortLinkUseCase {

    /**
     * 更新短链目标地址，并在事务提交后刷新运行时缓存。
     *
     * @param actorUserId 发起修改的用户
     * @param linkId 被修改的短链
     * @param fullUrl 新的绝对 HTTP 或 HTTPS URL
     * @throws ShortLinkNotFoundException 短链不存在时
     * @throws InvalidManagedTargetUrlException 目标地址不满足安全或格式约束时
     * @throws LinkManagementDeniedException IAM 明确拒绝修改时
     * @throws LinkManagementAuthorizationUnavailableException 无法获得 IAM 决策时
     */
    void updateTarget(UUID actorUserId, UUID linkId, String fullUrl);

    /**
     * 启用或停用短链，并在事务提交后刷新运行时缓存。
     *
     * @param actorUserId 发起修改的用户
     * @param linkId 被修改的短链
     * @param enabled {@code true} 表示允许解析，{@code false} 表示主动停用
     * @throws ShortLinkNotFoundException 短链不存在时
     * @throws LinkStateConflictException 已删除等生命周期状态不允许修改时
     * @throws LinkManagementDeniedException IAM 明确拒绝修改时
     * @throws LinkManagementAuthorizationUnavailableException 无法获得 IAM 决策时
     */
    void changeAvailability(UUID actorUserId, UUID linkId, boolean enabled);

    /**
     * 软删除短链，并在事务提交后使运行时缓存失效。
     *
     * @param actorUserId 发起删除的用户
     * @param linkId 被删除的短链
     * @throws ShortLinkNotFoundException 短链不存在或已经删除时
     * @throws LinkManagementDeniedException IAM 明确拒绝删除时
     * @throws LinkManagementAuthorizationUnavailableException 无法获得 IAM 决策时
     */
    void delete(UUID actorUserId, UUID linkId);
}
