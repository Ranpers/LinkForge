package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.security.application.port.in.ChangeUserSecurityStatusUseCase;
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

import static io.github.ranpers.linkforge.iam.control.adapter.in.web.ControlEventTraceHeaders.TRACE_ID;

@RestController
@RequestMapping("/api/v1/users/{userId}/security-status")
public class UserSecurityStatusController {

    private final ChangeUserSecurityStatusUseCase securityStatus;

    public UserSecurityStatusController(ChangeUserSecurityStatusUseCase securityStatus) {
        this.securityStatus = securityStatus;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void change(
            JwtAuthenticationToken authentication,
            @PathVariable UUID userId,
            @RequestBody ChangeUserSecurityStatusRequest request,
            @RequestHeader(value = TRACE_ID, required = false) String traceId
    ) {
        securityStatus.change(
                UUID.fromString(authentication.getToken().getSubject()),
                userId,
                request.suspended(),
                ControlEventTraceId.fromNullable(traceId)
        );
    }
}
