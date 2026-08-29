package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GrantProjectionMapper {

    void insertPlaceholders(@Param("pairs") List<AffectedPair> pairs);

    List<GrantStateRow> selectLocked(@Param("pairs") List<AffectedPair> pairs);

    void saveFlip(
            @Param("userId") UUID userId,
            @Param("domainId") UUID domainId,
            @Param("granted") boolean granted,
            @Param("revision") long revision
    );

    boolean isGranted(@Param("userId") UUID userId, @Param("domainId") UUID domainId);

    void insertOutbox(
            @Param("id") UUID id,
            @Param("eventType") String eventType,
            @Param("streamKey") String streamKey,
            @Param("partitionKey") String partitionKey,
            @Param("payload") String payload
    );
}
