package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.UUID;

public record ShortDomainRuntimeState(UUID shortDomainId, boolean enabled, long revision) {
}
