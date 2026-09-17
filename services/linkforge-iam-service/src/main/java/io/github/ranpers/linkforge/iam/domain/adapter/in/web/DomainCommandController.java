package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.domain.application.port.in.CreateDomainCommand;
import io.github.ranpers.linkforge.iam.domain.application.port.in.CreateDomainUseCase;
import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.in.UpdateDomainCommand;
import io.github.ranpers.linkforge.iam.domain.application.port.in.UpdateDomainUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.github.ranpers.linkforge.iam.control.adapter.in.web.ControlEventRequestHeaders.REQUEST_ID;

/**
 * 域名的写侧入口。
 *
 * <p>本控制器只做协议转换：权限判定在写入语句内部完成，因此这里不预检权限，
 * 由 {@link io.github.ranpers.linkforge.iam.domain.application.DomainWriteDeniedException}
 * 统一映射为 403。</p>
 */
@RestController
@RequestMapping("/api/v1/domains")
public class DomainCommandController {

    private final CreateDomainUseCase createDomain;
    private final UpdateDomainUseCase updateDomain;

    public DomainCommandController(
            CreateDomainUseCase createDomain,
            UpdateDomainUseCase updateDomain
    ) {
        this.createDomain = createDomain;
        this.updateDomain = updateDomain;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DomainListItem create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody CreateDomainRequest request,
            @RequestHeader(value = REQUEST_ID, required = false) String requestId
    ) {
        return createDomain.create(new CreateDomainCommand(
                UUID.fromString(authentication.getToken().getSubject()),
                request.domain(),
                request.name(),
                ControlEventRequestId.fromNullable(requestId)
        ));
    }

    @PatchMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            JwtAuthenticationToken authentication,
            @PathVariable UUID domainId,
            @Valid @RequestBody UpdateDomainRequest request
    ) {
        updateDomain.update(new UpdateDomainCommand(
                UUID.fromString(authentication.getToken().getSubject()),
                domainId,
                request.name()
        ));
    }
}
