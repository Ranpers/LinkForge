package io.github.ranpers.linkforge.iam.grant.application.port.out;

import java.util.UUID;

public interface LinkCreationAuthorizationQuery {

    LinkCreationAuthorizationSnapshot load(UUID userId, UUID domainId);
}
