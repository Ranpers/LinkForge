package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginUserMapper {

    LoginUserRow findByUsername(@Param("username") String username);
}
