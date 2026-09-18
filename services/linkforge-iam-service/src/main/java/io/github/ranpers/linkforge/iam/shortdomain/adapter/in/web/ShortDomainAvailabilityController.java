package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ChangeShortDomainAvailabilityUseCase;
import io.github.ranpers.linkforge.webmvc.request.RequestIdContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/short-domains")
public class ShortDomainAvailabilityController {

    private final ChangeShortDomainAvailabilityUseCase changeAvailability;

    public ShortDomainAvailabilityController(ChangeShortDomainAvailabilityUseCase changeAvailability) {
        this.changeAvailability = changeAvailability;
    }

    @PatchMapping("/{shortDomainId}/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void change(
            JwtAuthenticationToken authentication,
            @PathVariable UUID shortDomainId,
            @Valid @RequestBody ChangeShortDomainAvailabilityRequest request
    ) {
        changeAvailability.change(
                UUID.fromString(authentication.getToken().getSubject()),
                shortDomainId,
                request.enabled(),
                ControlEventRequestId.fromNullable(RequestIdContext.currentRequestId())
        );
    }
}
