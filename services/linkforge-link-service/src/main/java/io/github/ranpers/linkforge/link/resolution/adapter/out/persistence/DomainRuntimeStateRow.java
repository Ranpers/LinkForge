package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import java.util.UUID;

public record DomainRuntimeStateRow(UUID domainId, boolean enabled, long revision) {
}
