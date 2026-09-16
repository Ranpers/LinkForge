package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.application.port.in.RegisteredUser;

import java.util.UUID;

public record RegisterResponse(UUID id, String username, String email) {

    static RegisterResponse from(RegisteredUser registered) {
        return new RegisterResponse(registered.id(), registered.username(), registered.email());
    }
}
