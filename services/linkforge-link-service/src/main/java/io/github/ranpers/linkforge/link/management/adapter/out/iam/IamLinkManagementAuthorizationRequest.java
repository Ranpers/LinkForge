package io.github.ranpers.linkforge.link.management.adapter.out.iam;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAction;

import java.util.UUID;

public record IamLinkManagementAuthorizationRequest(
        UUID actorUserId,
        UUID domainId,
        UUID createdByUserId,
        LinkManagementAction action
) {
}
