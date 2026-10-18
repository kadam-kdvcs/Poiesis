package org.kdvcs.poiesis.machine.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineBatchTransactionTest {
    @Test
    void doesNotMutateWhenPreconditionFails() {
        int[] state = {10};
        boolean committed = MachineBatchTransaction.builder()
                .require(() -> false)
                .watch(() -> state[0], value -> state[0] = value)
                .mutate(() -> {
                    state[0] = 0;
                    return true;
                })
                .build()
                .commit();

        assertFalse(committed);
        assertEquals(10, state[0]);
    }

    @Test
    void rollsBackEveryWatchedResourceWhenMutationFails() {
        int[] item = {8};
        int[] fluid = {64_000};
        boolean committed = MachineBatchTransaction.builder()
                .watch(() -> item[0], value -> item[0] = value)
                .watch(() -> fluid[0], value -> fluid[0] = value)
                .mutate(() -> {
                    item[0] -= 3;
                    return true;
                })
                .mutate(() -> {
                    fluid[0] -= 8_000;
                    return false;
                })
                .build()
                .commit();

        assertFalse(committed);
        assertEquals(8, item[0]);
        assertEquals(64_000, fluid[0]);
    }

    @Test
    void rollsBackBeforePropagatingUnexpectedException() {
        int[] state = {4};
        MachineBatchTransaction transaction = MachineBatchTransaction.builder()
                .watch(() -> state[0], value -> state[0] = value)
                .mutate(() -> {
                    state[0] = 1;
                    throw new IllegalStateException("test");
                })
                .build();

        assertThrows(IllegalStateException.class, transaction::commit);
        assertEquals(4, state[0]);
    }

    @Test
    void keepsSuccessfulMutations() {
        int[] state = {5};
        boolean committed = MachineBatchTransaction.builder()
                .watch(() -> state[0], value -> state[0] = value)
                .mutate(() -> {
                    state[0] = 2;
                    return true;
                })
                .build()
                .commit();

        assertTrue(committed);
        assertEquals(2, state[0]);
    }
}
