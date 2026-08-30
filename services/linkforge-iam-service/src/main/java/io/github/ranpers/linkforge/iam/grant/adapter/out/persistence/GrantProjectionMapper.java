package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface GrantProjectionMapper {

    void createAffectedPairBuffer();

    void clearAffectedPairBuffer();

    int stagePairs(@Param("pairs") List<AffectedPair> pairs);

    void insertPlaceholdersFromBuffer();

    void lockStagedGrantStates();

    int reconcileAndInsertOutbox(@Param("occurredAt") OffsetDateTime occurredAt);

    boolean isGranted(@Param("userId") UUID userId, @Param("domainId") UUID domainId);
}
