package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantBatchProjectionStore;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrantBatchEngineTest {

    private final GrantBatchProjectionStore store = mock(GrantBatchProjectionStore.class);
    private final GrantBatchEngine engine = new GrantBatchEngine(store);

    @Test
    void shouldExecuteBatchStagesInStrictOrder() {
        List<String> steps = new ArrayList<>();
        doAnswer(_ -> add(steps, "reset")).when(store).resetBatch();
        doAnswer(_ -> add(steps, "pair-lock")).when(store).prepareAndLock();
        when(store.reconcileAndAppend(any(OffsetDateTime.class))).thenAnswer(_ -> {
            steps.add("reconcile");
            return 2;
        });

        GrantBatchResult result = engine.execute(new GrantChangePlan(
                () -> steps.add("topology-lock"),
                () -> {
                    steps.add("stage-impact");
                    return 3;
                },
                () -> steps.add("mutation")
        ));

        assertEquals(
                List.of("topology-lock", "reset", "stage-impact", "pair-lock", "mutation", "reconcile"),
                steps
        );
        assertEquals(new GrantBatchResult(3, 2), result);
    }

    @Test
    void shouldStillMutateWhenImpactSetIsEmpty() {
        int[] mutations = {0};

        GrantBatchResult result = engine.execute(new GrantChangePlan(
                () -> {
                },
                () -> 0,
                () -> mutations[0]++
        ));

        assertEquals(1, mutations[0]);
        assertEquals(GrantBatchResult.empty(), result);
        verify(store, never()).prepareAndLock();
        verify(store, never()).reconcileAndAppend(any());
    }

    @Test
    void shouldStopBeforeReconcileWhenMutationFails() {
        assertThrows(
                IllegalStateException.class,
                () -> engine.execute(new GrantChangePlan(
                        () -> {
                        },
                        () -> 1,
                        () -> {
                            throw new IllegalStateException("绑定变更失败");
                        }
                ))
        );

        verify(store).prepareAndLock();
        verify(store, never()).reconcileAndAppend(any());
    }

    @Test
    void shouldRejectInvalidImpactCount() {
        assertThrows(
                IllegalStateException.class,
                () -> engine.execute(new GrantChangePlan(() -> {
                }, () -> -1, () -> {
                }))
        );
        verify(store, never()).prepareAndLock();
    }

    private static Void add(List<String> steps, String step) {
        steps.add(step);
        return null;
    }
}
