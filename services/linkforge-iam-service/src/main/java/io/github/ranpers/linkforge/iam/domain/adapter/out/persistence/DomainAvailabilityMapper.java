package io.github.ranpers.linkforge.iam.domain.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface DomainAvailabilityMapper {
    Integer change(
            @Param("actorUserId") UUID actorUserId,
            @Param("domainId") UUID domainId,
            @Param("enabled") boolean enabled,
            @Param("traceId") String traceId
    );
}
