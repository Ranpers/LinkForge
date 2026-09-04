package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;
import io.github.ranpers.linkforge.iam.security.application.port.in.ManageLinkSecurityRestrictionUseCase;
import io.github.ranpers.linkforge.iam.security.domain.LinkSecurityRestriction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/link-security-restrictions")
public class LinkSecurityRestrictionController {

    private final ManageLinkSecurityRestrictionUseCase restrictions;

    public LinkSecurityRestrictionController(ManageLinkSecurityRestrictionUseCase restrictions) {
        this.restrictions = restrictions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLinkSecurityRestrictionResponse create(
            JwtAuthenticationToken authentication,
            @PathVariable UUID userId,
            @Valid @RequestBody CreateLinkSecurityRestrictionRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    ) {
        UUID restrictionId = restrictions.create(new CreateLinkSecurityRestrictionCommand(
                UUID.fromString(authentication.getToken().getSubject()),
                userId,
                new LinkSecurityRestriction(
                        request.mode(),
                        request.rangeStart(),
                        request.rangeEnd(),
                        request.reasonCode()
                ),
                traceId
        ));
        return new CreateLinkSecurityRestrictionResponse(restrictionId);
    }

    @DeleteMapping("/{restrictionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            JwtAuthenticationToken authentication,
            @PathVariable UUID userId,
            @PathVariable UUID restrictionId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    ) {
        restrictions.revoke(
                UUID.fromString(authentication.getToken().getSubject()),
                userId,
                restrictionId,
                traceId
        );
    }
}
