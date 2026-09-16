package io.github.ranpers.linkforge.link.query.adapter.out.persistence;

import io.github.ranpers.linkforge.link.query.application.port.in.ShortLinkListItem;
import io.github.ranpers.linkforge.link.query.application.port.out.ShortLinkListCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShortLinkListMapper {
    List<ShortLinkListItem> find(@Param("criteria") ShortLinkListCriteria criteria);
}
