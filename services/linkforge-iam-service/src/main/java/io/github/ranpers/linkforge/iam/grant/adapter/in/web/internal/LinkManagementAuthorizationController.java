package io.github.ranpers.linkforge.iam.grant.adapter.in.web.internal;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAuthorization;
import io.github.ranpers.linkforge.iam.grant.application.port.in.ValidateLinkManagementAuthorizationUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/authorizations")
public class LinkManagementAuthorizationController {

    private final ValidateLinkManagementAuthorizationUseCase authorization;

    public LinkManagementAuthorizationController(
            ValidateLinkManagementAuthorizationUseCase authorization
    ) {
        this.authorization = authorization;
    }

    @PostMapping("/link-management")
    public LinkManagementAuthorizationResponse validate(
            @Valid @RequestBody LinkManagementAuthorizationRequest request
    ) {
        LinkManagementAuthorization result = authorization.validate(
                request.actorUserId(),
                request.domainId(),
                request.createdByUserId(),
                request.action()
        );
        return new LinkManagementAuthorizationResponse(
                result.allowed(),
                result.reasonCode(),
                result.decisionId(),
                result.evaluatedAt()
        );
    }
}
