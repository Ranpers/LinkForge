package io.github.ranpers.linkforge.link.control.adapter.out.persistence;

import io.github.ranpers.linkforge.link.control.domain.LinkSecurityRestriction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface LinkControlEventMapper {

    int insertInbox(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType,
            @Param("schemaVersion") int schemaVersion,
            @Param("streamKey") String streamKey,
            @Param("traceId") String traceId
    );

    int insertCheckpointIfAbsent(@Param("streamKey") String streamKey);

    Long lockCheckpoint(@Param("streamKey") String streamKey);

    int advanceCheckpoint(@Param("streamKey") String streamKey, @Param("revision") long revision);

    int upsertDomainState(
            @Param("domainId") UUID domainId,
            @Param("host") String host,
            @Param("enabled") boolean enabled,
            @Param("revision") long revision
    );

    int deleteUserRestrictions(@Param("userId") UUID userId);

    int insertUserRestrictions(
            @Param("userId") UUID userId,
            @Param("revision") long revision,
            @Param("restrictions") List<LinkSecurityRestriction> restrictions
    );
}
