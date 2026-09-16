package io.github.ranpers.linkforge.link.resolution.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface LinkResolutionMapper {
    LinkResolutionRow findLinkByRoute(
            @Param("host") String host,
            @Param("linkCode") String linkCode
    );

    LinkResolutionRow findLinkById(@Param("linkId") UUID linkId);

    DomainRuntimeStateRow findDomain(@Param("domainId") UUID domainId);

    Long findRestrictionRevision(@Param("streamKey") String streamKey);

    List<UserSecurityRestrictionRow> findRestrictions(@Param("userId") UUID userId);
}
