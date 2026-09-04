package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper
public interface LinkSecurityRestrictionMapper {
    Boolean actorAllowed(@Param("actorUserId") UUID actorUserId);

    Long lockTargetUser(@Param("targetUserId") UUID targetUserId);

    UUID insertRestriction(
            @Param("targetUserId") UUID targetUserId,
            @Param("mode") String mode,
            @Param("rangeStart") OffsetDateTime rangeStart,
            @Param("rangeEnd") OffsetDateTime rangeEnd,
            @Param("reasonCode") String reasonCode
    );

    Boolean restrictionActive(
            @Param("targetUserId") UUID targetUserId,
            @Param("restrictionId") UUID restrictionId
    );

    int revokeRestriction(
            @Param("targetUserId") UUID targetUserId,
            @Param("restrictionId") UUID restrictionId
    );

    Long incrementRevision(@Param("targetUserId") UUID targetUserId);

    int appendSnapshotEvent(
            @Param("targetUserId") UUID targetUserId,
            @Param("revision") long revision,
            @Param("traceId") String traceId
    );
}
