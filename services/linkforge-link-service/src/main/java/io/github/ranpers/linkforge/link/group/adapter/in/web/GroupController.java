package io.github.ranpers.linkforge.link.group.adapter.in.web;

import io.github.ranpers.linkforge.link.group.application.port.in.CreateGroupCommand;
import io.github.ranpers.linkforge.link.group.application.port.in.CreateGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.DeleteGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.GetGroupUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.GroupPage;
import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.in.ListGroupsQuery;
import io.github.ranpers.linkforge.link.group.application.port.in.ListGroupsUseCase;
import io.github.ranpers.linkforge.link.group.application.port.in.UpdateGroupCommand;
import io.github.ranpers.linkforge.link.group.application.port.in.UpdateGroupUseCase;
import io.github.ranpers.linkforge.webmvc.pagination.CursorCodec;
import io.github.ranpers.linkforge.webmvc.pagination.CursorPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final CreateGroupUseCase createGroup;
    private final UpdateGroupUseCase updateGroup;
    private final DeleteGroupUseCase deleteGroup;
    private final GetGroupUseCase getGroup;
    private final ListGroupsUseCase listGroups;

    public GroupController(
            CreateGroupUseCase createGroup,
            UpdateGroupUseCase updateGroup,
            DeleteGroupUseCase deleteGroup,
            GetGroupUseCase getGroup,
            ListGroupsUseCase listGroups
    ) {
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.deleteGroup = deleteGroup;
        this.getGroup = getGroup;
        this.listGroups = listGroups;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupView create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        return createGroup.create(new CreateGroupCommand(
                actorId(authentication),
                request.name(),
                request.sortOrder() == null ? 0 : request.sortOrder()
        ));
    }

    @GetMapping
    public CursorPageResponse<GroupView> list(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
        GroupPage page = listGroups.list(new ListGroupsQuery(
                actorId(authentication),
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

    @GetMapping("/{groupId}")
    public GroupView get(
            JwtAuthenticationToken authentication,
            @PathVariable UUID groupId
    ) {
        return getGroup.get(actorId(authentication), groupId);
    }

    @PatchMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            JwtAuthenticationToken authentication,
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        updateGroup.update(new UpdateGroupCommand(
                actorId(authentication),
                groupId,
                request.name(),
                request.sortOrder()
        ));
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            JwtAuthenticationToken authentication,
            @PathVariable UUID groupId
    ) {
        deleteGroup.delete(actorId(authentication), groupId);
    }

    private static UUID actorId(JwtAuthenticationToken authentication) {
        return UUID.fromString(authentication.getToken().getSubject());
    }
}
