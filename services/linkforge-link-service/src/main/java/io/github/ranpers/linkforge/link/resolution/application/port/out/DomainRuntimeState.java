package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.UUID;

public record DomainRuntimeState(UUID domainId, boolean enabled, long revision) {
}
