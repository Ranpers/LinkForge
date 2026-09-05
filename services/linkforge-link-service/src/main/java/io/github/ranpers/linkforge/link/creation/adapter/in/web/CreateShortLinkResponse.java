package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreatedShortLink;
import io.github.ranpers.linkforge.link.creation.domain.ShortCodeType;

import java.util.UUID;

public record CreateShortLinkResponse(
        UUID id,
        String linkCode,
        UUID domainId,
        ShortCodeType codeType
) {
    static CreateShortLinkResponse from(CreatedShortLink link) {
        return new CreateShortLinkResponse(
                link.id(),
                link.linkCode(),
                link.domainId(),
                link.codeType()
        );
    }
}
