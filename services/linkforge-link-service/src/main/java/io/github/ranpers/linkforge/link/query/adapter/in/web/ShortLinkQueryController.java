package io.github.ranpers.linkforge.link.query.adapter.in.web;

import io.github.ranpers.linkforge.link.infrastructure.web.CursorCodec;
import io.github.ranpers.linkforge.link.infrastructure.web.CursorPageResponse;
import io.github.ranpers.linkforge.link.query.application.port.in.ListShortLinksQuery;
import io.github.ranpers.linkforge.link.query.application.port.in.ListShortLinksUseCase;
import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListItem;
import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListStatus;
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
@RequestMapping("/api/v1/links")
public class ShortLinkQueryController {

    private final ListShortLinksUseCase listShortLinks;

    public ShortLinkQueryController(ListShortLinksUseCase listShortLinks) {
        this.listShortLinks = listShortLinks;
    }

    @GetMapping
    public CursorPageResponse<ShortLinkListItem> list(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) UUID createdByUserId,
            @RequestParam(required = false) UUID domainId,
            @RequestParam(required = false) ShortLinkListStatus status,
            @RequestParam(required = false) @Size(max = 64) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        UUID actorUserId = UUID.fromString(authentication.getToken().getSubject());
        List<String> permissions = authentication.getToken().getClaimAsStringList("permissions");
        boolean canReadAll = permissions != null && permissions.contains("link:manage:any");
        CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
        var page = listShortLinks.list(new ListShortLinksQuery(
                actorUserId,
                canReadAll,
                createdByUserId,
                domainId,
                status,
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
