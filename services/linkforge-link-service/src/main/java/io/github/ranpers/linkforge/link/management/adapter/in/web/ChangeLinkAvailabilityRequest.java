package io.github.ranpers.linkforge.link.management.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record ChangeLinkAvailabilityRequest(@NotNull Boolean enabled) {
}
