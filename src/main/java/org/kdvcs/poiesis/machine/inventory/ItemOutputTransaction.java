package org.kdvcs.poiesis.machine.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Plans and commits a complete batch of internal machine item outputs.
 *
 * <p>All requested stacks share one simulated inventory snapshot. Existing
 * compatible stacks are filled first, then remainders are split across empty
 * output slots. A plan is only committed when every requested item fits and
 * the output slots still match the snapshot used to create the plan.</p>
 *
 * <p>This intentionally bypasses {@link IItemHandler#isItemValid(int, ItemStack)}
 * when committing. Machine output slots commonly reject external insertion,
 * while still allowing the machine itself to place recipe results there.</p>
 */
public final class ItemOutputTransaction {
    private ItemOutputTransaction() {
    }

    public static boolean canFit(
            IItemHandler handler,
            int firstSlot,
            int endSlotExclusive,
            Iterable<ItemStack> outputs
    ) {
        return plan(handler, firstSlot, endSlotExclusive, outputs).isPresent();
    }

    public static Optional<Plan> plan(
            IItemHandler handler,
            int firstSlot,
            int endSlotExclusive,
            Iterable<ItemStack> outputs
    ) {
        if (handler == null) {
            throw new IllegalArgumentException("Item output handler cannot be null");
        }
        if (outputs == null) {
            throw new IllegalArgumentException("Item outputs cannot be null");
        }
        if (firstSlot < 0
                || endSlotExclusive < firstSlot
                || endSlotExclusive > handler.getSlots()) {
            throw new IllegalArgumentException(
                    "Invalid item output slot range ["
                            + firstSlot + ", " + endSlotExclusive + ")"
            );
        }

        List<ItemStack> expected = snapshot(
                handler,
                firstSlot,
                endSlotExclusive
        );
        List<ItemStack> simulated = copyStacks(expected);

        for (ItemStack output : outputs) {
            if (output == null) {
                throw new IllegalArgumentException("Item output cannot be null");
            }
            if (output.isEmpty()) {
                continue;
            }
            if (!insertIntoSimulation(
                    handler,
                    firstSlot,
                    output,
                    simulated
            )) {
                return Optional.empty();
            }
        }

        return Optional.of(new Plan(
                firstSlot,
                endSlotExclusive,
                expected,
                simulated
        ));
    }

    private static boolean insertIntoSimulation(
            IItemHandler handler,
            int firstSlot,
            ItemStack requested,
            List<ItemStack> simulated
    ) {
        int remaining = requested.getCount();

        // Match Forge's stacked insertion behavior: fill compatible stacks
        // before consuming empty slots.
        for (int offset = 0; offset < simulated.size() && remaining > 0; offset++) {
            ItemStack stored = simulated.get(offset);
            if (stored.isEmpty()
                    || !ItemStack.isSameItemSameTags(stored, requested)) {
                continue;
            }

            int capacity = stackCapacity(
                    handler,
                    firstSlot + offset,
                    requested
            );
            int accepted = Math.min(
                    remaining,
                    Math.max(0, capacity - stored.getCount())
            );
            if (accepted <= 0) {
                continue;
            }

            ItemStack merged = stored.copy();
            merged.grow(accepted);
            simulated.set(offset, merged);
            remaining -= accepted;
        }

        for (int offset = 0; offset < simulated.size() && remaining > 0; offset++) {
            if (!simulated.get(offset).isEmpty()) {
                continue;
            }

            int capacity = stackCapacity(
                    handler,
                    firstSlot + offset,
                    requested
            );
            int accepted = Math.min(remaining, capacity);
            if (accepted <= 0) {
                continue;
            }

            ItemStack placed = requested.copy();
            placed.setCount(accepted);
            simulated.set(offset, placed);
            remaining -= accepted;
        }

        return remaining == 0;
    }

    private static int stackCapacity(
            IItemHandler handler,
            int slot,
            ItemStack stack
    ) {
        return Math.max(
                0,
                Math.min(handler.getSlotLimit(slot), stack.getMaxStackSize())
        );
    }

    private static List<ItemStack> snapshot(
            IItemHandler handler,
            int firstSlot,
            int endSlotExclusive
    ) {
        List<ItemStack> stacks = new ArrayList<>(endSlotExclusive - firstSlot);
        for (int slot = firstSlot; slot < endSlotExclusive; slot++) {
            stacks.add(handler.getStackInSlot(slot).copy());
        }
        return stacks;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copies.add(stack.copy());
        }
        return copies;
    }

    private static boolean stacksEqual(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.getCount() == second.getCount()
                && ItemStack.isSameItemSameTags(first, second);
    }

    public static final class Plan {
        private final int firstSlot;
        private final int endSlotExclusive;
        private final List<ItemStack> expected;
        private final List<ItemStack> result;

        private Plan(
                int firstSlot,
                int endSlotExclusive,
                List<ItemStack> expected,
                List<ItemStack> result
        ) {
            this.firstSlot = firstSlot;
            this.endSlotExclusive = endSlotExclusive;
            this.expected = copyStacks(expected);
            this.result = copyStacks(result);
        }

        /**
         * Returns whether none of the planned output slots changed since the
         * simulation was created.
         */
        public boolean matches(IItemHandler handler) {
            if (handler == null || handler.getSlots() < endSlotExclusive) {
                return false;
            }
            for (int offset = 0; offset < expected.size(); offset++) {
                if (!stacksEqual(
                        expected.get(offset),
                        handler.getStackInSlot(firstSlot + offset)
                )) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Commits the complete simulated state, or changes nothing if the
         * output inventory no longer matches the original snapshot.
         */
        public boolean commit(IItemHandlerModifiable handler) {
            if (!matches(handler)) {
                return false;
            }

            for (int offset = 0; offset < result.size(); offset++) {
                ItemStack current = handler.getStackInSlot(firstSlot + offset);
                ItemStack planned = result.get(offset);
                if (!stacksEqual(current, planned)) {
                    handler.setStackInSlot(
                            firstSlot + offset,
                            planned.copy()
                    );
                }
            }
            return true;
        }
    }
}
