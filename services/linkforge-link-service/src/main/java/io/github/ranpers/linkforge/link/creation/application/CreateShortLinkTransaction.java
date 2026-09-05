package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkIdGenerator;
import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkRepository;
import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * 将短链创建所需的数据库操作限制在不包含远程调用的短事务内。
 *
 * @implNote 调用者必须在进入写事务前完成 IAM 授权。写事务会重新检查幂等键和分组归属，
 * 防止授权等待期间发生的并发创建或分组状态变化绕过本地约束。
 */
@Service
public class CreateShortLinkTransaction {

    private final ShortLinkRepository repository;
    private final ShortLinkIdGenerator idGenerator;

    public CreateShortLinkTransaction(
            ShortLinkRepository repository,
            ShortLinkIdGenerator idGenerator
    ) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    /**
     * 在调用 IAM 前查找可直接返回的幂等重放结果。
     *
     * @param command 非空的创建命令
     * @param fingerprint 当前命令的 SHA-256 请求指纹
     * @return 找到相同幂等请求时返回原创建结果；尚未创建时为空
     * @throws IdempotencyConflictException 幂等键已用于不同请求内容时
     */
    @Transactional(readOnly = true)
    public Optional<CreatedShortLink> findReplay(
            CreateShortLinkCommand command,
            String fingerprint
    ) {
        return findReplayInCurrentTransaction(command, fingerprint);
    }

    /**
     * 检查分组当前是否仍属于指定用户。
     *
     * @param groupId 非空的分组标识
     * @param userId 非空的预期所有者
     * @return 分组存在、属于用户且未删除时返回 {@code true}
     */
    @Transactional(readOnly = true)
    public boolean groupBelongsToUser(UUID groupId, UUID userId) {
        return repository.groupBelongsToUser(groupId, userId);
    }

    /**
     * 在 IAM 已允许创建后原子地完成本地校验和持久化。
     *
     * @param command IAM 已评估的创建命令
     * @param fingerprint 当前命令的 SHA-256 请求指纹
     * @return 新创建或并发幂等重放的短链
     * @throws IdempotencyConflictException 并发请求复用了幂等键但请求内容不同时
     * @throws InvalidLinkGroupException 分组在写入前已不再属于操作者时
     */
    @Transactional
    public CreatedShortLink createAuthorized(
            CreateShortLinkCommand command,
            String fingerprint
    ) {
        Optional<CreatedShortLink> replay =
                findReplayInCurrentTransaction(command, fingerprint);
        if (replay.isPresent()) {
            return replay.get();
        }
        if (command.groupId() != null
                && !repository.groupBelongsToUser(command.groupId(), command.actorUserId())) {
            throw new InvalidLinkGroupException();
        }

        ShortLink link = new ShortLink(
                idGenerator.nextId(),
                command.actorUserId(),
                command.groupId(),
                command.name(),
                command.linkCode(),
                command.fullUrl(),
                command.sortOrder(),
                command.domainId(),
                command.expiresAt(),
                command.idempotencyKey(),
                fingerprint,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        if (repository.insertIfIdempotencyAbsent(link)) {
            return CreatedShortLink.from(link);
        }
        return findReplayInCurrentTransaction(command, fingerprint)
                .orElseThrow(() -> new IllegalStateException(
                        "幂等约束冲突后未找到原创建结果"
                ));
    }

    private Optional<CreatedShortLink> findReplayInCurrentTransaction(
            CreateShortLinkCommand command,
            String fingerprint
    ) {
        return repository.findByIdempotencyKey(
                        command.actorUserId(), command.idempotencyKey())
                .map(existing -> resolveReplay(existing, fingerprint));
    }

    private static CreatedShortLink resolveReplay(ShortLink existing, String fingerprint) {
        if (!existing.requestFingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException();
        }
        return CreatedShortLink.from(existing);
    }
}
