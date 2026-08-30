package io.github.ranpers.linkforge.link.availability.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface AuthorizationEventMapper {

    int insertInbox(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType,
            @Param("streamKey") String streamKey
    );

    int insertCheckpointIfAbsent(@Param("streamKey") String streamKey);

    Long lockCheckpoint(@Param("streamKey") String streamKey);

    int advanceCheckpoint(
            @Param("streamKey") String streamKey,
            @Param("revision") long revision
    );

    int applyDomainAvailability(
            @Param("domainId") UUID domainId,
            @Param("enabled") boolean enabled
    );

    int applyUserAvailability(
            @Param("userId") UUID userId,
            @Param("enabled") boolean enabled
    );

    int applyUserDomainGrant(
            @Param("userId") UUID userId,
            @Param("domainId") UUID domainId,
            @Param("granted") boolean granted
    );
}
