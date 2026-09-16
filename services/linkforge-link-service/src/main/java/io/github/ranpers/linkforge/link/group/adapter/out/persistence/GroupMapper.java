package io.github.ranpers.linkforge.link.group.adapter.out.persistence;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface GroupMapper {

    UUID insert(
            @Param("userId") UUID userId,
            @Param("name") String name,
            @Param("sortOrder") int sortOrder
    );

    GroupView find(@Param("userId") UUID userId, @Param("groupId") UUID groupId);

    Integer update(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId,
            @Param("name") String name,
            @Param("sortOrder") Integer sortOrder
    );

    UUID lockActiveOwned(@Param("userId") UUID userId, @Param("groupId") UUID groupId);

    int softDelete(@Param("userId") UUID userId, @Param("groupId") UUID groupId);
}
