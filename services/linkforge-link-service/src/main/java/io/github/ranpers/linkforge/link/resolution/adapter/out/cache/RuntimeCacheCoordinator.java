package io.github.ranpers.linkforge.link.resolution.adapter.out.cache;

import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCache;
import io.github.ranpers.linkforge.link.control.domain.ShortDomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.ShortDomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestriction;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * 将数据库事务状态变化与 Redis 写屏障的生命周期关联。
 *
 * @implNote 事务提交后用捕获的新事实完成屏障；事务回滚时仅撤销屏障。完成动作失败时，
 * 数据键保持缺失并由屏障 TTL 保证最终恢复为数据库回源。
 */
@Component
public class RuntimeCacheCoordinator implements LinkControlCache, LinkManagementCache {

    private static final Logger log = LoggerFactory.getLogger(RuntimeCacheCoordinator.class);

    private final LinkRuntimeFactSource source;
    private final LinkRuntimeCache cache;

    public RuntimeCacheCoordinator(LinkRuntimeFactSource source, LinkRuntimeCache cache) {
        this.source = source;
        this.cache = cache;
    }

    @Override
    public void synchronizeMutation(LinkControlEvent event) {
        switch (event) {
            case ShortDomainAvailabilityChanged shortDomain -> synchronizeShortDomain(shortDomain);
            case UserLinkSecurityRestrictionsChanged user -> synchronizeRestrictions(user);
        }
    }

    @Override
    public void synchronizeMutation(UUID linkId) {
        var current = source.findLink(linkId)
                .orElseThrow(() -> new IllegalStateException("缓存同步目标短链不存在: " + linkId));
        String token = cache.beginLinkMutation(current.host(), current.linkCode());
        afterCompletion(
                () -> cache.completeLinkMutation(current, token),
                () -> cache.cancelLinkMutation(current.host(), current.linkCode(), token)
        );
    }

    private void synchronizeShortDomain(ShortDomainAvailabilityChanged event) {
        ShortDomainRuntimeState shortDomain = new ShortDomainRuntimeState(
                event.shortDomainId(), event.enabled(), event.revision()
        );
        String token = cache.beginShortDomainMutation(event.shortDomainId());
        afterCompletion(
                () -> cache.completeShortDomainMutation(shortDomain, token),
                () -> cache.cancelShortDomainMutation(event.shortDomainId(), token)
        );
    }

    private void synchronizeRestrictions(UserLinkSecurityRestrictionsChanged event) {
        UserSecurityRestrictionSet restrictions = new UserSecurityRestrictionSet(
                event.userId(),
                event.revision(),
                event.restrictions().stream()
                        .map(rule -> new UserSecurityRestriction(
                                rule.restrictionId(),
                                rule.mode().name(),
                                rule.rangeStart(),
                                rule.rangeEnd(),
                                rule.reasonCode(),
                                rule.createdAt()
                        ))
                        .toList()
        );
        String token = cache.beginRestrictionsMutation(event.userId());
        afterCompletion(
                () -> cache.completeRestrictionsMutation(restrictions, token),
                () -> cache.cancelRestrictionsMutation(event.userId(), token)
        );
    }

    private static void afterCompletion(Runnable committedAction, Runnable rolledBackAction) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("运行时缓存同步必须在活动事务中注册");
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        Runnable action = status == TransactionSynchronization.STATUS_COMMITTED
                                ? committedAction
                                : rolledBackAction;
                        try {
                            action.run();
                        } catch (RuntimeException exception) {
                            log.error("运行时缓存事务收尾失败，写屏障将由 TTL 自动释放", exception);
                        }
                    }
                }
        );
    }
}
