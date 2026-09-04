package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAction;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkManagementAuthorizationRequest(
        @NotNull UUID actorUserId,
        @NotNull UUID domainId,
        @NotNull UUID createdByUserId,
        @NotNull LinkManagementAction action
) {
}
