package io.github.ranpers.linkforge.link.creation.adapter.out.id;

import io.github.ranpers.linkforge.link.creation.application.port.out.ShortLinkIdGenerator;
import io.github.ranpers.linkforge.link.infrastructure.id.UuidV7;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidV7ShortLinkIdGenerator implements ShortLinkIdGenerator {
    @Override
    public UUID nextId() {
        return UuidV7.generate();
    }
}
