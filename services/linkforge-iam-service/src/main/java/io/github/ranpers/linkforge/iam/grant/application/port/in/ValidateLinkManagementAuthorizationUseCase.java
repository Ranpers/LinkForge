package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.util.UUID;

public interface ValidateLinkManagementAuthorizationUseCase {
    LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    );
}
