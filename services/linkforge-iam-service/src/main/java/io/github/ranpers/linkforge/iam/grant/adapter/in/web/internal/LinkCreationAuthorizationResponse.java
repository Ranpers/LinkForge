package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkCreationAuthorization;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LinkCreationAuthorizationResponse(
        boolean allowed,
        String reasonCode,
        UUID decisionId,
        OffsetDateTime evaluatedAt
) {
    static LinkCreationAuthorizationResponse from(LinkCreationAuthorization authorization) {
        return new LinkCreationAuthorizationResponse(
                authorization.allowed(),
                authorization.reasonCode(),
                authorization.decisionId(),
                authorization.evaluatedAt()
        );
    }
}
