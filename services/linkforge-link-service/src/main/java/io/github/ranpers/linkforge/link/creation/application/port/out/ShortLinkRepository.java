package io.github.ranpers.linkforge.link.creation.application.port.out;

import io.github.ranpers.linkforge.link.creation.domain.ShortCode;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;

import java.util.Optional;
import java.util.UUID;

/**
 * 持久化短链，并暴露创建事务识别已知唯一约束冲突所需的事实。
 */
public interface ShortLinkRepository {

    /**
     * 查找操作者范围内已完成的幂等创建结果。
     *
     * @param userId         创建者的非空标识
     * @param idempotencyKey 非空且不超过 128 个字符的幂等键
     * @return 已存在时返回原短链，否则返回空
     */
    Optional<ShortLink> findByIdempotencyKey(UUID userId, String idempotencyKey);

    /**
     * 判断活动分组是否属于指定用户。
     *
     * @param groupId 非空的分组标识
     * @param userId  非空的预期所有者标识
     * @return 分组存在、未软删除且属于用户时为 {@code true}
     */
    boolean groupBelongsToUser(UUID groupId, UUID userId);

    /**
     * 尝试写入短链并忽略唯一约束冲突。
     *
     * @param link 已建立领域不变量的非空短链
     * @return 成功插入时为 {@code true}；任一唯一约束阻止插入时为 {@code false}
     * @apiNote 返回 {@code false} 后，调用者必须依次检查幂等键、内部标识和域名短码，
     * 无法归类时按持久化不变量错误处理
     */
    boolean tryInsert(ShortLink link);

    /**
     * 判断内部标识是否已经被占用。
     *
     * @param linkId 非空的 UUIDv7 短链标识
     * @return 任意短链占用该标识时为 {@code true}，包括已软删除记录
     */
    boolean existsById(UUID linkId);

    /**
     * 判断域名内短码是否已经被永久占用。
     *
     * @param domainId  非空的域名标识
     * @param shortCode 非空的候选短码
     * @return 任意短链占用该组合时为 {@code true}，包括已软删除记录
     */
    boolean existsByDomainAndCode(UUID domainId, ShortCode shortCode);
}
