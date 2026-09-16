package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.in.ListDomainsQuery;
import io.github.ranpers.linkforge.iam.domain.application.port.in.ListDomainsUseCase;
import io.github.ranpers.linkforge.iam.infrastructure.web.CursorCodec;
import io.github.ranpers.linkforge.iam.infrastructure.web.CursorPageResponse;
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
@RequestMapping("/api/v1/domains")
public class DomainQueryController {

    private final ListDomainsUseCase listDomains;

    public DomainQueryController(ListDomainsUseCase listDomains) {
        this.listDomains = listDomains;
    }

    @GetMapping
    public CursorPageResponse<DomainListItem> list(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) @Size(max = 128) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        UUID actorUserId = UUID.fromString(authentication.getToken().getSubject());
        List<String> permissions = authentication.getToken().getClaimAsStringList("permissions");
        boolean canReadAll = permissions != null && permissions.contains("domain:read");
        CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
        var page = listDomains.list(new ListDomainsQuery(
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
