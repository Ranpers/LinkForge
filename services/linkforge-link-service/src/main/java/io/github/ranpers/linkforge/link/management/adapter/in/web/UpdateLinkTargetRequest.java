package io.github.ranpers.linkforge.link.management.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLinkTargetRequest(
        @NotBlank @Size(max = 2048) String fullUrl
) {
}
