package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import java.util.UUID;

public record ShortDomainRuntimeStateRow(UUID shortDomainId, boolean enabled, long revision) {
}
