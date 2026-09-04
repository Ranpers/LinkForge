package io.github.ranpers.linkforge.link.management.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface LinkManagementMapper {
    LinkManagementRow find(@Param("linkId") UUID linkId);

    int updateTarget(@Param("linkId") UUID linkId, @Param("fullUrl") String fullUrl);

    Integer changeAvailability(
            @Param("linkId") UUID linkId,
            @Param("enabled") boolean enabled
    );

    int softDelete(@Param("linkId") UUID linkId);
}
