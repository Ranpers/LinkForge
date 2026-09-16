package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record ChangeDomainAvailabilityRequest(@NotNull Boolean enabled) {
}
