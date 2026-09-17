package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.webmvc.pagination.CursorCodec;
import io.github.ranpers.linkforge.webmvc.pagination.CursorPageResponse;
import io.github.ranpers.linkforge.iam.user.application.port.in.ListUsersQuery;
import io.github.ranpers.linkforge.iam.user.application.port.in.ListUsersUseCase;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserListItem;
import io.github.ranpers.linkforge.iam.user.application.port.in.UserListStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserQueryController {

    private final ListUsersUseCase listUsers;

    public UserQueryController(ListUsersUseCase listUsers) {
        this.listUsers = listUsers;
    }

    @GetMapping
    public CursorPageResponse<UserListItem> list(
            @RequestParam(required = false) UserListStatus status,
            @RequestParam(required = false) @Size(max = 64) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
        var page = listUsers.list(new ListUsersQuery(
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
