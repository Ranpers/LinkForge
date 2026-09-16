package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAction;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationQuery;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationSnapshot;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisLinkManagementAuthorizationQuery
        implements LinkManagementAuthorizationQuery {

    private final LinkAuthorizationMapper mapper;

    public MybatisLinkManagementAuthorizationQuery(LinkAuthorizationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LinkManagementAuthorizationSnapshot load(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    ) {
        LinkManagementAuthorizationRow row = mapper.findManagementSnapshot(
                actorUserId,
                domainId,
                createdByUserId,
                action.permissionCode()
        );
        if (row == null) {
            throw new IllegalStateException("短链管理授权快照查询没有返回结果");
        }
        return new LinkManagementAuthorizationSnapshot(
                row.userEnabled(),
                row.domainEnabled(),
                row.globalManagementAllowed(),
                row.ownManagementAllowed()
        );
    }
}
