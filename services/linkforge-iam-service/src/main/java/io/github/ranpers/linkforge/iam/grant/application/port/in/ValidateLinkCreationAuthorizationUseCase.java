package io.github.ranpers.linkforge.iam.grant.application.port.in;

import java.util.UUID;

public interface ValidateLinkCreationAuthorizationUseCase {

    LinkCreationAuthorization validate(UUID userId, UUID domainId);
}
