package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MybatisGrantBatchProjectionStoreTest {

    @Test
    void shouldStageExplicitPairsInBoundedChunks() {
        GrantProjectionMapper mapper = mock(GrantProjectionMapper.class);
        when(mapper.stagePairs(anyList())).thenAnswer(invocation -> invocation.<List<?>>getArgument(0).size());
        MybatisGrantBatchProjectionStore store = new MybatisGrantBatchProjectionStore(mapper);
        List<AffectedPair> pairs = new ArrayList<>();
        for (int index = 0; index < 1_201; index++) {
            pairs.add(new AffectedPair(UUID.randomUUID(), UUID.randomUUID()));
        }

        assertEquals(1_201, store.stagePairs(pairs));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AffectedPair>> chunks = ArgumentCaptor.forClass(List.class);
        verify(mapper, times(3)).stagePairs(chunks.capture());
        assertEquals(List.of(500, 500, 201), chunks.getAllValues().stream().map(List::size).toList());
    }
}
