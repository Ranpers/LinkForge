package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface LinkAuthorizationMapper {

    LinkCreationAuthorizationRow findSnapshot(
            @Param("userId") UUID userId,
            @Param("domainId") UUID domainId
    );

    LinkManagementAuthorizationRow findManagementSnapshot(
            @Param("userId") UUID actorUserId,
            @Param("domainId") UUID domainId,
            @Param("createdByUserId") UUID createdByUserId,
            @Param("actionPermission") String actionPermission
    );
}
