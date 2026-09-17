package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.domain.application.port.in.ChangeDomainAvailabilityUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.github.ranpers.linkforge.iam.control.adapter.in.web.ControlEventRequestHeaders.REQUEST_ID;

@RestController
@RequestMapping("/api/v1/domains")
public class DomainAvailabilityController {

    private final ChangeDomainAvailabilityUseCase changeAvailability;

    public DomainAvailabilityController(ChangeDomainAvailabilityUseCase changeAvailability) {
        this.changeAvailability = changeAvailability;
    }

    @PatchMapping("/{domainId}/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void change(
            JwtAuthenticationToken authentication,
            @PathVariable UUID domainId,
            @Valid @RequestBody ChangeDomainAvailabilityRequest request,
            @RequestHeader(value = REQUEST_ID, required = false) String requestId
    ) {
        changeAvailability.change(
                UUID.fromString(authentication.getToken().getSubject()),
                domainId,
                request.enabled(),
                ControlEventRequestId.fromNullable(requestId)
        );
    }
}
