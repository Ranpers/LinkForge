package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.GrantSnapshot;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantProjectionStore;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MybatisGrantProjectionStore implements GrantProjectionStore {

    private final GrantProjectionMapper mapper;

    public MybatisGrantProjectionStore(GrantProjectionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<AffectedPair, GrantSnapshot> lockAndLoad(List<AffectedPair> orderedPairs) {
        mapper.insertPlaceholders(orderedPairs);
        return mapper.selectLocked(orderedPairs).stream()
                .collect(Collectors.toMap(
                        row -> new AffectedPair(row.getUserId(), row.getDomainId()),
                        row -> new GrantSnapshot(row.isGranted(), row.getRevision()),
                        (a, _) -> a,
                        HashMap::new
                ));
    }

    @Override
    public void saveFlip(AffectedPair pair, boolean granted, long revision) {
        mapper.saveFlip(pair.userId(), pair.domainId(), granted, revision);
    }
}
