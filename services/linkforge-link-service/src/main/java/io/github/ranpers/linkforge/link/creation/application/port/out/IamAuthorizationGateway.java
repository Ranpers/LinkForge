package io.github.ranpers.linkforge.link.creation.application.port.out;

import io.github.ranpers.linkforge.link.creation.application.LinkCreationAuthorization;

import java.util.UUID;

public interface IamAuthorizationGateway {

    LinkCreationAuthorization validate(UUID userId, UUID domainId);
}
