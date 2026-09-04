package io.github.ranpers.linkforge.link.management.adapter.in.web;

import io.github.ranpers.linkforge.link.management.application.port.in.ManageShortLinkUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/links/{linkId}")
public class ShortLinkManagementController {

    private final ManageShortLinkUseCase management;

    public ShortLinkManagementController(ManageShortLinkUseCase management) {
        this.management = management;
    }

    @PatchMapping("/target")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTarget(
            JwtAuthenticationToken authentication,
            @PathVariable UUID linkId,
            @Valid @RequestBody UpdateLinkTargetRequest request
    ) {
        management.updateTarget(actorId(authentication), linkId, request.fullUrl());
    }

    @PatchMapping("/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeAvailability(
            JwtAuthenticationToken authentication,
            @PathVariable UUID linkId,
            @RequestBody ChangeLinkAvailabilityRequest request
    ) {
        management.changeAvailability(actorId(authentication), linkId, request.enabled());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            JwtAuthenticationToken authentication,
            @PathVariable UUID linkId
    ) {
        management.delete(actorId(authentication), linkId);
    }

    private static UUID actorId(JwtAuthenticationToken authentication) {
        return UUID.fromString(authentication.getToken().getSubject());
    }
}
