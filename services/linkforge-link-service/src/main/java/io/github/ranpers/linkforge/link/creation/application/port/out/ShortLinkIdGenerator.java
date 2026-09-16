package io.github.ranpers.linkforge.link.creation.application.port.out;

import java.util.UUID;

public interface ShortLinkIdGenerator {

    UUID nextId();
}
