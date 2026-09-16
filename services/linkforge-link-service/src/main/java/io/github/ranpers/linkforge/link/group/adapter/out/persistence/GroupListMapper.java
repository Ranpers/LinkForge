package io.github.ranpers.linkforge.link.group.adapter.out.persistence;

import io.github.ranpers.linkforge.link.group.application.port.in.GroupView;
import io.github.ranpers.linkforge.link.group.application.port.out.GroupListCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupListMapper {
    List<GroupView> find(@Param("criteria") GroupListCriteria criteria);
}
