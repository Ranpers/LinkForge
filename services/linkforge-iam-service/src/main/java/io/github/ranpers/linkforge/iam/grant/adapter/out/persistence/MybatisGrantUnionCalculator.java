package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantUnionCalculator;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisGrantUnionCalculator implements GrantUnionCalculator {

    private final GrantProjectionMapper mapper;

    public MybatisGrantUnionCalculator(GrantProjectionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isGranted(UUID userId, UUID domainId) {
        return mapper.isGranted(userId, domainId);
    }
}
