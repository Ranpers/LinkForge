package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import io.github.ranpers.linkforge.iam.security.domain.RestrictionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateLinkSecurityRestrictionRequest(
        @NotNull RestrictionMode mode,
        OffsetDateTime rangeStart,
        OffsetDateTime rangeEnd,
        @NotBlank @Size(max = 64) String reasonCode
) {
}
