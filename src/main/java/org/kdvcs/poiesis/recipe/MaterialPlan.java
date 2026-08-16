package org.kdvcs.poiesis.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

/**
 * Finds an atomic consumption plan for counted ingredients.
 *
 * A max-flow formulation is used so overlapping Ingredient predicates do not
 * greedily consume a slot that is required by another ingredient.
 */
public final class MaterialPlan {
    private MaterialPlan() {
    }

    public static int[] find(
            IItemHandler inventory,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        validateRange(inventory.getSlots(), startSlot, endSlotExclusive);
        return find(
                slot -> inventory.getStackInSlot(startSlot + slot),
                endSlotExclusive - startSlot,
                requirements
        );
    }

    public static int[] find(
            Container container,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        validateRange(container.getContainerSize(), startSlot, endSlotExclusive);
        return find(
                slot -> container.getItem(startSlot + slot),
                endSlotExclusive - startSlot,
                requirements
        );
    }

    public static boolean canSatisfy(
            IItemHandler inventory,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        return find(inventory, startSlot, endSlotExclusive, requirements) != null;
    }

    public static boolean canSatisfy(
            Container container,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        return find(container, startSlot, endSlotExclusive, requirements) != null;
    }

    private static int[] find(
            StackSource stacks,
            int slotCount,
            List<CountedIngredient> requirements
    ) {
        if (requirements == null || requirements.isEmpty()) {
            return new int[slotCount];
        }

        int ingredientCount = requirements.size();
        int source = 0;
        int ingredientStart = 1;
        int slotStart = ingredientStart + ingredientCount;
        int sink = slotStart + slotCount;
        int nodeCount = sink + 1;

        int[][] capacity = new int[nodeCount][nodeCount];
        int totalRequired = 0;

        for (int ingredient = 0; ingredient < ingredientCount; ingredient++) {
            CountedIngredient requirement = requirements.get(ingredient);
            int required = requirement.count();
            capacity[source][ingredientStart + ingredient] = required;
            totalRequired += required;

            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = stacks.get(slot);
                if (!stack.isEmpty() && requirement.ingredient().test(stack)) {
                    capacity[ingredientStart + ingredient][slotStart + slot] = required;
                }
            }
        }

        for (int slot = 0; slot < slotCount; slot++) {
            capacity[slotStart + slot][sink] = stacks.get(slot).getCount();
        }

        int[][] flow = new int[nodeCount][nodeCount];
        int maxFlow = 0;

        while (true) {
            int[] parent = new int[nodeCount];
            Arrays.fill(parent, -1);
            parent[source] = source;

            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int from = queue.removeFirst();

                for (int to = 0; to < nodeCount; to++) {
                    if (parent[to] == -1
                            && capacity[from][to] - flow[from][to] > 0) {
                        parent[to] = from;
                        queue.addLast(to);
                    }
                }
            }

            if (parent[sink] == -1) {
                break;
            }

            int augment = Integer.MAX_VALUE;
            for (int node = sink; node != source; node = parent[node]) {
                int previous = parent[node];
                augment = Math.min(
                        augment,
                        capacity[previous][node] - flow[previous][node]
                );
            }

            for (int node = sink; node != source; node = parent[node]) {
                int previous = parent[node];
                flow[previous][node] += augment;
                flow[node][previous] -= augment;
            }

            maxFlow += augment;
        }

        if (maxFlow != totalRequired) {
            return null;
        }

        int[] consumePerSlot = new int[slotCount];
        for (int ingredient = 0; ingredient < ingredientCount; ingredient++) {
            for (int slot = 0; slot < slotCount; slot++) {
                int used = flow[ingredientStart + ingredient][slotStart + slot];
                if (used > 0) {
                    consumePerSlot[slot] += used;
                }
            }
        }

        return consumePerSlot;
    }

    private static void validateRange(
            int size,
            int startSlot,
            int endSlotExclusive
    ) {
        if (startSlot < 0
                || endSlotExclusive < startSlot
                || endSlotExclusive > size) {
            throw new IndexOutOfBoundsException(
                    "Invalid material slot range [" + startSlot + ", "
                            + endSlotExclusive + ") for size " + size
            );
        }
    }

    @FunctionalInterface
    private interface StackSource {
        ItemStack get(int relativeSlot);
    }
}
