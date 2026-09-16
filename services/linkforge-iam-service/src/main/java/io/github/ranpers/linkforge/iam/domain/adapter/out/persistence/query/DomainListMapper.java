package io.github.ranpers.linkforge.iam.domain.adapter.out.persistence.query;

import io.github.ranpers.linkforge.iam.domain.application.port.in.DomainListItem;
import io.github.ranpers.linkforge.iam.domain.application.port.out.DomainListCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DomainListMapper {
    List<DomainListItem> find(@Param("criteria") DomainListCriteria criteria);
}
