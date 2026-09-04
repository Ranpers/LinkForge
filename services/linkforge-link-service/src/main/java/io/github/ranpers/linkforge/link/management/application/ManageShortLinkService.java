package io.github.ranpers.linkforge.link.management.application;

import io.github.ranpers.linkforge.link.management.application.port.in.ManageShortLinkUseCase;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementAuthorizationGateway;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementCache;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementRepository;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementSnapshot;
import io.github.ranpers.linkforge.link.management.domain.ManagedTargetUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ManageShortLinkService implements ManageShortLinkUseCase {

    private final LinkManagementRepository repository;
    private final LinkManagementAuthorizationGateway authorization;
    private final LinkManagementCache cache;

    public ManageShortLinkService(
            LinkManagementRepository repository,
            LinkManagementAuthorizationGateway authorization,
            LinkManagementCache cache
    ) {
        this.repository = repository;
        this.authorization = authorization;
        this.cache = cache;
    }

    @Override
    @Transactional
    public void updateTarget(UUID actorUserId, UUID linkId, String fullUrl) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.UPDATE);
        ManagedTargetUrl target = new ManagedTargetUrl(fullUrl);
        if (!repository.updateTarget(linkId, target.value())) {
            throw new ShortLinkNotFoundException();
        }
        cache.refreshAfterCommit(linkId);
    }

    @Override
    @Transactional
    public void changeAvailability(UUID actorUserId, UUID linkId, boolean enabled) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.UPDATE);
        switch (repository.changeAvailability(linkId, enabled)) {
            case CHANGED, UNCHANGED -> {
            }
            case FORBIDDEN_STATE -> throw new LinkStateConflictException();
            case NOT_FOUND -> throw new ShortLinkNotFoundException();
        }
        cache.refreshAfterCommit(linkId);
    }

    @Override
    @Transactional
    public void delete(UUID actorUserId, UUID linkId) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.DELETE);
        if (!repository.softDelete(linkId)) {
            throw new ShortLinkNotFoundException();
        }
        cache.refreshAfterCommit(linkId);
    }

    private LinkManagementSnapshot requireLink(UUID linkId) {
        LinkManagementSnapshot link = repository.find(linkId);
        if (link == null) {
            throw new ShortLinkNotFoundException();
        }
        return link;
    }

    private void authorize(
            UUID actorUserId,
            LinkManagementSnapshot link,
            LinkManagementAction action
    ) {
        LinkManagementAuthorization decision = authorization.validate(
                actorUserId,
                link.domainId(),
                link.createdByUserId(),
                action
        );
        if (!decision.allowed()) {
            throw new LinkManagementDeniedException(
                    decision.reasonCode(),
                    decision.decisionId()
            );
        }
    }
}
