package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventRequestId;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.CreateShortDomainCommand;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.CreateShortDomainUseCase;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.UpdateShortDomainCommand;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.UpdateShortDomainUseCase;
import io.github.ranpers.linkforge.webmvc.request.RequestIdContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 域名的写侧入口。
 *
 * <p>本控制器只做协议转换：权限判定在写入语句内部完成，因此这里不预检权限，
 * 由 {@link io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainWriteDeniedException}
 * 统一映射为 403。</p>
 */
@RestController
@RequestMapping("/api/v1/short-domains")
public class ShortDomainCommandController {

    private final CreateShortDomainUseCase createShortDomain;
    private final UpdateShortDomainUseCase updateShortDomain;

    public ShortDomainCommandController(
            CreateShortDomainUseCase createShortDomain,
            UpdateShortDomainUseCase updateShortDomain
    ) {
        this.createShortDomain = createShortDomain;
        this.updateShortDomain = updateShortDomain;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortDomainListItem create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody CreateShortDomainRequest request
    ) {
        return createShortDomain.create(new CreateShortDomainCommand(
                UUID.fromString(authentication.getToken().getSubject()),
                request.host(),
                request.name(),
                ControlEventRequestId.fromNullable(RequestIdContext.currentRequestId())
        ));
    }

    @PatchMapping("/{shortDomainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            JwtAuthenticationToken authentication,
            @PathVariable UUID shortDomainId,
            @Valid @RequestBody UpdateShortDomainRequest request
    ) {
        updateShortDomain.update(new UpdateShortDomainCommand(
                UUID.fromString(authentication.getToken().getSubject()),
                shortDomainId,
                request.name()
        ));
    }
}
