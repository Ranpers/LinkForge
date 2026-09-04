package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkCreationAuthorizationRequest(
        @NotNull UUID userId,
        @NotNull UUID domainId
) {
}
