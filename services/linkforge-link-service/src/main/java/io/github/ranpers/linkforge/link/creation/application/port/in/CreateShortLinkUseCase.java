package io.github.ranpers.linkforge.link.creation.application.port.in;

import io.github.ranpers.linkforge.link.creation.application.IamAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.creation.application.IdempotencyConflictException;
import io.github.ranpers.linkforge.link.creation.application.InvalidLinkGroupException;
import io.github.ranpers.linkforge.link.creation.application.LinkCreationDeniedException;
import io.github.ranpers.linkforge.link.creation.domain.InvalidShortLinkException;

/**
 * 经 IAM 授权后持久化新短链。
 */
public interface CreateShortLinkUseCase {

    /**
     * 创建短链，或在幂等键和请求内容均相同时返回第一次创建的结果。
     *
     * @param command 已通过传输层基本校验的创建输入
     * @return 新创建或幂等重放的短链标识
     * @throws InvalidShortLinkException 短码、目标地址或过期时间违反领域约束时
     * @throws InvalidLinkGroupException 指定分组不属于操作者时
     * @throws LinkCreationDeniedException IAM 明确拒绝创建时
     * @throws IamAuthorizationUnavailableException 无法获得 IAM 决策时
     * @throws IdempotencyConflictException 同一幂等键已用于不同请求内容时
     */
    CreatedShortLink create(CreateShortLinkCommand command);
}
