package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface UserRoleMapper {
    int insertByRoleCode(
            @Param("userId") UUID userId,
            @Param("roleCode") String roleCode
    );
}
