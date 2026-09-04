package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkCreationAuthorizationQuery;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkCreationAuthorizationSnapshot;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisLinkCreationAuthorizationQuery implements LinkCreationAuthorizationQuery {

    private final LinkAuthorizationMapper mapper;

    public MybatisLinkCreationAuthorizationQuery(LinkAuthorizationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LinkCreationAuthorizationSnapshot load(UUID userId, UUID domainId) {
        LinkCreationAuthorizationRow row = mapper.findSnapshot(userId, domainId);
        if (row == null) {
            throw new IllegalStateException("单条授权快照查询没有返回结果");
        }
        return new LinkCreationAuthorizationSnapshot(
                row.userEnabled(),
                row.actionAllowed(),
                row.domainEnabled(),
                row.domainGranted()
        );
    }
}
