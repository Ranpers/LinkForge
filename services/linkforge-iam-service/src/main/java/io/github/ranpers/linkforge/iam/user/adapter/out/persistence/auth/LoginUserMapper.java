package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface LoginUserMapper {

    LoginUserRow findByUsername(@Param("username") String username);

    List<String> findRoleCodes(@Param("userId") UUID userId);

    List<String> findPermissionCodes(@Param("userId") UUID userId);
}
