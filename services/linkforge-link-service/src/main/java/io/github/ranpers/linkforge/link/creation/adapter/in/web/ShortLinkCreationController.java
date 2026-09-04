package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/links")
public class ShortLinkCreationController {

    private final CreateShortLinkUseCase createShortLink;

    public ShortLinkCreationController(CreateShortLinkUseCase createShortLink) {
        this.createShortLink = createShortLink;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateShortLinkResponse create(
            JwtAuthenticationToken authentication,
            @RequestHeader("Idempotency-Key")
            @NotBlank @Size(max = 128) String idempotencyKey,
            @Valid @RequestBody CreateShortLinkRequest request
    ) {
        UUID actorUserId = UUID.fromString(authentication.getToken().getSubject());
        return CreateShortLinkResponse.from(createShortLink.create(
                new CreateShortLinkCommand(
                        actorUserId,
                        request.groupId(),
                        request.name(),
                        request.linkCode(),
                        request.fullUrl(),
                        request.sortOrder(),
                        request.domainId(),
                        request.expiresAt(),
                        idempotencyKey
                )
        ));
    }
}
