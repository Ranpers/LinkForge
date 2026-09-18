package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface ShortDomainAvailabilityMapper {
    Integer change(
            @Param("actorUserId") UUID actorUserId,
            @Param("shortDomainId") UUID shortDomainId,
            @Param("enabled") boolean enabled,
            @Param("requestId") UUID requestId
    );
}
