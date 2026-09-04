package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateShortLinkRequest(
        UUID groupId,
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(min = 4, max = 64) String linkCode,
        @NotBlank @Size(max = 2048) String fullUrl,
        int sortOrder,
        @NotNull UUID domainId,
        OffsetDateTime expiresAt
) {
}
