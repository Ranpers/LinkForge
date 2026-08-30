package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantBatchProjectionStore;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class MybatisGrantBatchProjectionStore implements GrantBatchProjectionStore {

    private static final int INSERT_CHUNK_SIZE = 500;

    private final GrantProjectionMapper mapper;

    public MybatisGrantBatchProjectionStore(GrantProjectionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void resetBatch() {
        mapper.createAffectedPairBuffer();
        mapper.clearAffectedPairBuffer();
    }

    @Override
    public int stagePairs(List<AffectedPair> pairs) {
        int staged = 0;
        for (int start = 0; start < pairs.size(); start += INSERT_CHUNK_SIZE) {
            int end = Math.min(start + INSERT_CHUNK_SIZE, pairs.size());
            staged += mapper.stagePairs(pairs.subList(start, end));
        }
        return staged;
    }

    @Override
    public void prepareAndLock() {
        mapper.insertPlaceholdersFromBuffer();
        mapper.lockStagedGrantStates();
    }

    @Override
    public int reconcileAndAppend(OffsetDateTime occurredAt) {
        return mapper.reconcileAndInsertOutbox(occurredAt);
    }
}
