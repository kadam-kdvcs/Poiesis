package org.kdvcs.poiesis.machine.transaction;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Small atomic boundary for a machine recipe batch.
 *
 * <p>All preconditions are evaluated before any mutation. Watched resources
 * are then snapshotted once and restored in reverse order if a mutation does
 * not commit completely. Recipe-specific matching stays in the process; this
 * class owns only the common transaction mechanics.</p>
 */
public final class MachineBatchTransaction {
    private final List<BooleanSupplier> preconditions;
    private final List<SnapshotSource<?>> snapshots;
    private final List<BooleanSupplier> mutations;

    private MachineBatchTransaction(Builder builder) {
        preconditions = List.copyOf(builder.preconditions);
        snapshots = List.copyOf(builder.snapshots);
        mutations = List.copyOf(builder.mutations);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean commit() {
        for (BooleanSupplier precondition : preconditions) {
            if (!precondition.getAsBoolean()) {
                return false;
            }
        }

        List<Snapshot<?>> captured = new ArrayList<>(snapshots.size());
        for (SnapshotSource<?> source : snapshots) {
            captured.add(source.captureSnapshot());
        }

        try {
            for (BooleanSupplier mutation : mutations) {
                if (!mutation.getAsBoolean()) {
                    restore(captured);
                    return false;
                }
            }
            return true;
        } catch (RuntimeException exception) {
            restore(captured);
            throw exception;
        }
    }

    private static void restore(List<Snapshot<?>> snapshots) {
        for (int index = snapshots.size() - 1; index >= 0; index--) {
            snapshots.get(index).restoreValue();
        }
    }

    public static final class Builder {
        private final List<BooleanSupplier> preconditions = new ArrayList<>();
        private final List<SnapshotSource<?>> snapshots = new ArrayList<>();
        private final List<BooleanSupplier> mutations = new ArrayList<>();

        public Builder require(BooleanSupplier precondition) {
            preconditions.add(precondition);
            return this;
        }

        public Builder mutate(BooleanSupplier mutation) {
            mutations.add(mutation);
            return this;
        }

        public <T> Builder watch(Supplier<T> capture, Consumer<T> restore) {
            snapshots.add(new SnapshotSource<>(capture, restore));
            return this;
        }

        public Builder watchItems(IItemHandlerModifiable items, int... slots) {
            int[] watchedSlots = Arrays.stream(slots).distinct().toArray();
            return watch(
                    () -> {
                        ItemStack[] values = new ItemStack[watchedSlots.length];
                        for (int index = 0; index < watchedSlots.length; index++) {
                            values[index] = items.getStackInSlot(watchedSlots[index]).copy();
                        }
                        return values;
                    },
                    values -> {
                        for (int index = 0; index < watchedSlots.length; index++) {
                            items.setStackInSlot(watchedSlots[index], values[index]);
                        }
                    }
            );
        }

        public Builder watchItemRange(
                IItemHandlerModifiable items,
                int startInclusive,
                int endExclusive
        ) {
            int[] slots = new int[Math.max(0, endExclusive - startInclusive)];
            for (int index = 0; index < slots.length; index++) {
                slots[index] = startInclusive + index;
            }
            return watchItems(items, slots);
        }

        public Builder watchFluid(FluidTank tank) {
            return watch(
                    () -> tank.getFluid().copy(),
                    value -> tank.setFluid(value.copy())
            );
        }

        public MachineBatchTransaction build() {
            return new MachineBatchTransaction(this);
        }
    }

    private record SnapshotSource<T>(Supplier<T> capture, Consumer<T> restore) {
        private Snapshot<T> captureSnapshot() {
            return new Snapshot<>(capture.get(), restore);
        }
    }

    private record Snapshot<T>(T value, Consumer<T> restore) {
        private void restoreValue() {
            restore.accept(value);
        }
    }
}
