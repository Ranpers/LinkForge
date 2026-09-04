package io.github.ranpers.linkforge.iam.grant.application.port.out;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAction;

import java.util.UUID;

public interface LinkManagementAuthorizationQuery {
    LinkManagementAuthorizationSnapshot load(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    );
}
