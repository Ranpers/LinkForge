package io.github.ranpers.linkforge.iam.domain.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface DomainStoreMapper {

    DomainCreationRow create(
            @Param("actorUserId") UUID actorUserId,
            @Param("host") String host,
            @Param("name") String name,
            @Param("traceId") String traceId
    );

    Integer update(
            @Param("actorUserId") UUID actorUserId,
            @Param("domainId") UUID domainId,
            @Param("name") String name
    );
}
