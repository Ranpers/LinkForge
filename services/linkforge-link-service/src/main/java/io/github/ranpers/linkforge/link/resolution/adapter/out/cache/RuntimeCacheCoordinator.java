package io.github.ranpers.linkforge.link.resolution.adapter.out.cache;

import io.github.ranpers.linkforge.link.control.application.port.out.LinkControlCache;
import io.github.ranpers.linkforge.link.control.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.DomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFactSource;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestriction;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
public class RuntimeCacheCoordinator implements LinkControlCache, LinkManagementCache {

    private final LinkRuntimeFactSource source;
    private final LinkRuntimeCache cache;

    public RuntimeCacheCoordinator(LinkRuntimeFactSource source, LinkRuntimeCache cache) {
        this.source = source;
        this.cache = cache;
    }

    @Override
    public void projectAfterCommit(LinkControlEvent event) {
        afterCommit(() -> {
            switch (event) {
                case DomainAvailabilityChanged domain -> cache.putDomain(
                        new DomainRuntimeState(
                                domain.domainId(),
                                domain.enabled(),
                                domain.revision()
                        )
                );
                case UserLinkSecurityRestrictionsChanged user -> cache.putRestrictions(
                        new UserSecurityRestrictionSet(
                                user.userId(),
                                user.revision(),
                                user.restrictions().stream()
                                        .map(rule -> new UserSecurityRestriction(
                                                rule.restrictionId(),
                                                rule.mode().name(),
                                                rule.rangeStart(),
                                                rule.rangeEnd(),
                                                rule.reasonCode(),
                                                rule.createdAt()
                                        ))
                                        .toList()
                        )
                );
            }
        });
    }

    @Override
    public void refreshAfterCommit(UUID linkId) {
        afterCommit(() -> source.findLink(linkId).ifPresent(cache::putLink));
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
}
