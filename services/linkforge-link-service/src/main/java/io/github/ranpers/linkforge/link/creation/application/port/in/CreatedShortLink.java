package io.github.ranpers.linkforge.link.creation.application.port.in;

import io.github.ranpers.linkforge.link.creation.domain.ShortLink;

import java.util.UUID;

public record CreatedShortLink(UUID id, String linkCode, UUID domainId) {

    public static CreatedShortLink from(ShortLink link) {
        return new CreatedShortLink(link.id(), link.linkCode(), link.domainId());
    }
}
