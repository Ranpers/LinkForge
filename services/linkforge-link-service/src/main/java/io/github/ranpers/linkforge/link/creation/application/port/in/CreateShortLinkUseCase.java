package io.github.ranpers.linkforge.link.creation.application.port.in;

public interface CreateShortLinkUseCase {

    CreatedShortLink create(CreateShortLinkCommand command);
}
