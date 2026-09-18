package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ListShortDomainsQuery;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ListShortDomainsUseCase;
import io.github.ranpers.linkforge.webmvc.pagination.CursorCodec;
import io.github.ranpers.linkforge.webmvc.pagination.CursorPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/short-domains")
public class ShortDomainQueryController {

    private final ListShortDomainsUseCase listShortDomains;

    public ShortDomainQueryController(ListShortDomainsUseCase listShortDomains) {
        this.listShortDomains = listShortDomains;
    }

    @GetMapping
    public CursorPageResponse<ShortDomainListItem> list(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) @Size(max = 128) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        UUID actorUserId = UUID.fromString(authentication.getToken().getSubject());
        List<String> permissions = authentication.getToken().getClaimAsStringList("permissions");
        boolean canReadAll = permissions != null && permissions.contains("short-domain:read");
        CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
        var page = listShortDomains.list(new ListShortDomainsQuery(
                actorUserId,
                canReadAll,
                enabled,
                q,
                position.createdAt(),
                position.id(),
                limit
        ));
        String nextCursor = page.hasMore()
                ? CursorCodec.encode(
                        page.items().getLast().createdAt(),
                        page.items().getLast().id()
                )
                : null;
        return new CursorPageResponse<>(page.items(), nextCursor, page.hasMore());
    }
}
