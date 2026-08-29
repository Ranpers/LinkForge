package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantOutboxStore;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantProjectionStore;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantUnionCalculator;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrantProjectionPipelineTest {

    private final GrantProjectionStore store = mock(GrantProjectionStore.class);
    private final GrantUnionCalculator calculator = mock(GrantUnionCalculator.class);
    private final GrantOutboxStore outbox = mock(GrantOutboxStore.class);
    private final GrantProjectionPipeline pipeline = new GrantProjectionPipeline(store, calculator, outbox);

    private final UUID userId = UUID.randomUUID();
    private final UUID domainId = UUID.randomUUID();

    @Test
    void shouldSortLockMutateRecomputeInOrder() {
        // 乱序输入 + 重复项,管道必须去重排序后加锁
        UUID otherDomainId = UUID.randomUUID();
        Collection<AffectedPair> unsorted = List.of(
                new AffectedPair(userId, domainId),
                new AffectedPair(userId, otherDomainId),
                new AffectedPair(userId, domainId)
        );
        when(store.lockAndLoad(any())).thenReturn(Map.of(
                new AffectedPair(userId, domainId), new GrantSnapshot(false, 0),
                new AffectedPair(userId, otherDomainId), new GrantSnapshot(false, 0)
        ));
        when(calculator.isGranted(any(), any())).thenReturn(false);

        pipeline.project(() -> {
        }, () -> unsorted, () -> {
            });

        verify(store).lockAndLoad(argThat(pairs -> {
            List<AffectedPair> list = List.copyOf(pairs);
            return list.size() == 2
                    && list.get(0).domainId().compareTo(list.get(1).domainId()) < 0;
        }));
        verify(store, never()).saveFlip(any(), any(Boolean.class), any(Long.class));
    }

    @Test
    void shouldRunMutationExactlyOnceBetweenLockAndRecompute() {
        AffectedPair pair = new AffectedPair(userId, domainId);
        List<String> steps = new ArrayList<>();
        when(store.lockAndLoad(any())).thenAnswer(_ -> {
            steps.add("pair-lock");
            return Map.of(pair, new GrantSnapshot(false, 0));
        });
        when(calculator.isGranted(userId, domainId)).thenAnswer(_ -> {
            steps.add("recompute");
            return true;
        });
        int[] mutations = {0};

        pipeline.project(
                () -> steps.add("topology-lock"),
                () -> {
                    steps.add("affected-pairs");
                    return List.of(pair);
                },
                () -> {
                    steps.add("mutation");
                    mutations[0]++;
                }
        );

        assertEquals(1, mutations[0]);
        assertEquals(
                List.of("topology-lock", "affected-pairs", "pair-lock", "mutation", "recompute"),
                steps
        );
    }

    @Test
    void shouldEmitFlipWithIncrementedRevision() {
        AffectedPair pair = new AffectedPair(userId, domainId);
        when(store.lockAndLoad(any())).thenReturn(Map.of(pair, new GrantSnapshot(false, 7)));
        when(calculator.isGranted(userId, domainId)).thenReturn(true);

        pipeline.project(() -> {
        }, () -> List.of(pair), () -> {
            });

        verify(store).saveFlip(pair, true, 8);
        verify(outbox).appendGrantChanged(eq(pair), eq(true), eq(8L), any(OffsetDateTime.class));
    }

    @Test
    void shouldEmitNothingWhenGrantUnchanged() {
        AffectedPair pair = new AffectedPair(userId, domainId);
        when(store.lockAndLoad(any())).thenReturn(Map.of(pair, new GrantSnapshot(true, 3)));
        when(calculator.isGranted(userId, domainId)).thenReturn(true);

        pipeline.project(() -> {
        }, () -> List.of(pair), () -> {
            });

        verify(store, never()).saveFlip(any(), any(Boolean.class), any(Long.class));
        verify(outbox, never()).appendGrantChanged(any(), any(Boolean.class), any(Long.class), any());
    }

    @Test
    void shouldEmitOnlyFlippedPairsInMixedSet() {
        AffectedPair revoked = new AffectedPair(userId, domainId);
        AffectedPair untouched = new AffectedPair(UUID.randomUUID(), UUID.randomUUID());
        when(store.lockAndLoad(any())).thenReturn(Map.of(
                revoked, new GrantSnapshot(true, 5),
                untouched, new GrantSnapshot(false, 0)
        ));
        when(calculator.isGranted(revoked.userId(), revoked.domainId())).thenReturn(false);
        when(calculator.isGranted(untouched.userId(), untouched.domainId())).thenReturn(false);

        pipeline.project(() -> {
        }, () -> List.of(revoked, untouched), () -> {
            });

        verify(store).saveFlip(revoked, false, 6);
        verify(outbox).appendGrantChanged(eq(revoked), eq(false), eq(6L), any(OffsetDateTime.class));
        verify(store, never()).saveFlip(untouched, false, 1);
    }

    @Test
    void shouldStillMutateWhenAffectedSetEmpty() {
        int[] mutations = {0};

        pipeline.project(() -> {
        }, List::of, () -> mutations[0]++);

        assertEquals(1, mutations[0]);
        verify(store, never()).lockAndLoad(any());
        verify(calculator, never()).isGranted(any(), any());
    }

    @Test
    void shouldComputeAffectedSetInsidePipelineCall() {
        boolean[] supplierInvoked = {false};

        pipeline.project(() -> {
        }, () -> {
            supplierInvoked[0] = true;
            return List.of();
        }, () -> {
        });

        assertTrue(supplierInvoked[0]);
    }

    @Test
    void shouldStopBeforeRecomputeWhenMutationThrows() {
        AffectedPair pair = new AffectedPair(userId, domainId);
        when(store.lockAndLoad(any())).thenReturn(Map.of(pair, new GrantSnapshot(false, 0)));
        when(calculator.isGranted(userId, domainId)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> pipeline.project(
                        () -> {
                        },
                        () -> List.of(pair),
                        () -> {
                            throw new IllegalStateException("绑定变更失败");
                        }
                )
        );

        verify(calculator, never()).isGranted(any(), any());
        verify(outbox, never()).appendGrantChanged(any(), any(Boolean.class), any(Long.class), any());
    }
}
