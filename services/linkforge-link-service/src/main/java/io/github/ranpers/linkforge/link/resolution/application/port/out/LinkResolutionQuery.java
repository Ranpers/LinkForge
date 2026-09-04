package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.Optional;

public interface LinkResolutionQuery {
    Optional<LinkResolutionSnapshot> find(String host, String linkCode);
}
