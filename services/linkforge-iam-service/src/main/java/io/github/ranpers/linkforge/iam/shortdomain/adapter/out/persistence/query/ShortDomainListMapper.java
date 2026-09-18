package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;
import io.github.ranpers.linkforge.iam.shortdomain.application.port.out.ShortDomainListCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShortDomainListMapper {
    List<ShortDomainListItem> find(@Param("criteria") ShortDomainListCriteria criteria);
}
