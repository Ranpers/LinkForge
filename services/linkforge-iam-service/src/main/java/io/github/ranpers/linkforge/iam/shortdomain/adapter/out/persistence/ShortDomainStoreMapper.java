package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface ShortDomainStoreMapper {

    ShortDomainCreationRow create(
            @Param("actorUserId") UUID actorUserId,
            @Param("host") String host,
            @Param("name") String name,
            @Param("requestId") UUID requestId
    );

    Integer update(
            @Param("actorUserId") UUID actorUserId,
            @Param("shortDomainId") UUID shortDomainId,
            @Param("name") String name
    );
}
