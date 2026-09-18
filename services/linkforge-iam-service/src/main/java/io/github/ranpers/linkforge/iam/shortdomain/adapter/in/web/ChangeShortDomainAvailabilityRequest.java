package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record ChangeShortDomainAvailabilityRequest(@NotNull Boolean enabled) {
}
