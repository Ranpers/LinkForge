package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record ChangeUserSecurityStatusRequest(@NotNull Boolean suspended) {
}
