package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import io.github.ranpers.linkforge.iam.grant.application.port.in.ValidateLinkCreationAuthorizationUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/authorizations")
public class LinkCreationAuthorizationController {

    private final ValidateLinkCreationAuthorizationUseCase authorization;

    public LinkCreationAuthorizationController(
            ValidateLinkCreationAuthorizationUseCase authorization
    ) {
        this.authorization = authorization;
    }

    @PostMapping("/link-creation")
    public LinkCreationAuthorizationResponse validateLinkCreation(
            @Valid @RequestBody LinkCreationAuthorizationRequest request
    ) {
        return LinkCreationAuthorizationResponse.from(
                authorization.validate(request.userId(), request.domainId())
        );
    }
}
