package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.user.application.port.in.UserListItem;
import io.github.ranpers.linkforge.iam.user.application.port.out.UserListCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserListMapper {
    List<UserListItem> find(@Param("criteria") UserListCriteria criteria);
}
