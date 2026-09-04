package io.github.ranpers.linkforge.link.management.application.port.out;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAction;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorization;

import java.util.UUID;

public interface LinkManagementAuthorizationGateway {
    LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    );
}
