package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAction;
import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAuthorization;
import io.github.ranpers.linkforge.iam.grant.application.port.in.ValidateLinkManagementAuthorizationUseCase;
import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationDecisionIdGenerator;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationQuery;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Service
public class LinkManagementAuthorizationService
        implements ValidateLinkManagementAuthorizationUseCase {

    private final LinkManagementAuthorizationQuery query;
    private final AuthorizationDecisionIdGenerator decisionIds;

    public LinkManagementAuthorizationService(
            LinkManagementAuthorizationQuery query,
            AuthorizationDecisionIdGenerator decisionIds
    ) {
        this.query = query;
        this.decisionIds = decisionIds;
    }

    @Override
    @Transactional(readOnly = true)
    public LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID shortDomainId,
            UUID createdByUserId,
            LinkManagementAction action
    ) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(shortDomainId, "shortDomainId");
        Objects.requireNonNull(createdByUserId, "createdByUserId");
        Objects.requireNonNull(action, "action");
        LinkManagementAuthorizationSnapshot snapshot =
                query.load(actorUserId, shortDomainId, createdByUserId, action);
        String reasonCode = reasonCode(snapshot);
        return new LinkManagementAuthorization(
                "ALLOWED".equals(reasonCode),
                reasonCode,
                decisionIds.nextId(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private static String reasonCode(LinkManagementAuthorizationSnapshot snapshot) {
        if (!snapshot.userEnabled()) {
            return "USER_NOT_AVAILABLE";
        }
        if (!snapshot.shortDomainEnabled()) {
            return "SHORT_DOMAIN_NOT_AVAILABLE";
        }
        if (!snapshot.globalManagementAllowed() && !snapshot.ownManagementAllowed()) {
            return "LINK_MANAGEMENT_NOT_ALLOWED";
        }
        return "ALLOWED";
    }
}
