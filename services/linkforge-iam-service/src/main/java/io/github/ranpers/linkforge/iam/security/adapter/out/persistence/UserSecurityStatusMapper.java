package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface UserSecurityStatusMapper {
    Integer change(
            @Param("actorUserId") UUID actorUserId,
            @Param("targetUserId") UUID targetUserId,
            @Param("suspended") boolean suspended
    );
}
