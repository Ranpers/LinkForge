package io.github.ranpers.linkforge.link.management.application;

import io.github.ranpers.linkforge.link.management.application.port.in.ManageShortLinkUseCase;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementAuthorizationGateway;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementSnapshot;
import io.github.ranpers.linkforge.link.management.domain.ManagedTargetUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ManageShortLinkService implements ManageShortLinkUseCase {

    private final LinkManagementAuthorizationGateway authorization;
    private final ManageShortLinkTransaction transaction;

    public ManageShortLinkService(
            LinkManagementAuthorizationGateway authorization,
            ManageShortLinkTransaction transaction
    ) {
        this.authorization = authorization;
        this.transaction = transaction;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateTarget(UUID actorUserId, UUID linkId, String fullUrl) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.UPDATE);
        ManagedTargetUrl target = new ManagedTargetUrl(fullUrl);
        if (!transaction.updateTarget(linkId, target.value())) {
            throw new ShortLinkNotFoundException();
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void changeAvailability(UUID actorUserId, UUID linkId, boolean enabled) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.UPDATE);
        switch (transaction.changeAvailability(linkId, enabled)) {
            case CHANGED, UNCHANGED -> {
            }
            case FORBIDDEN_STATE -> throw new LinkStateConflictException();
            case NOT_FOUND -> throw new ShortLinkNotFoundException();
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void delete(UUID actorUserId, UUID linkId) {
        LinkManagementSnapshot link = requireLink(linkId);
        authorize(actorUserId, link, LinkManagementAction.DELETE);
        if (!transaction.softDelete(linkId)) {
            throw new ShortLinkNotFoundException();
        }
    }

    private LinkManagementSnapshot requireLink(UUID linkId) {
        LinkManagementSnapshot link = transaction.find(linkId);
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
