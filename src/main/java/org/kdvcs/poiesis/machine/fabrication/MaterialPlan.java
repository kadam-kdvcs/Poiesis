package org.kdvcs.poiesis.machine.fabrication;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.recipe.CountedIngredient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class MaterialPlan {
    private MaterialPlan() {}

    public static int[] find(
            ItemStackHandler inventory,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        int ingredientCount = requirements.size();
        int slotCount = endSlotExclusive - startSlot;
        int source = 0;
        int ingredientStart = 1;
        int slotStart = ingredientStart + ingredientCount;
        int sink = slotStart + slotCount;
        int nodeCount = sink + 1;

        int[][] capacity = new int[nodeCount][nodeCount];
        int totalRequired = 0;

        for (int i = 0; i < ingredientCount; i++) {
            int required = requirements.get(i).count();
            capacity[source][ingredientStart + i] = required;
            totalRequired += required;

            for (int j = 0; j < slotCount; j++) {
                ItemStack stack = inventory.getStackInSlot(startSlot + j);
                if (!stack.isEmpty() && requirements.get(i).ingredient().test(stack)) {
                    capacity[ingredientStart + i][slotStart + j] = required;
                }
            }
        }

        for (int j = 0; j < slotCount; j++) {
            capacity[slotStart + j][sink] = inventory.getStackInSlot(startSlot + j).getCount();
        }

        int[][] flow = new int[nodeCount][nodeCount];
        int maxFlow = 0;

        while (true) {
            int[] parent = new int[nodeCount];
            java.util.Arrays.fill(parent, -1);
            parent[source] = source;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.removeFirst();
                for (int v = 0; v < nodeCount; v++) {
                    if (parent[v] == -1 && capacity[u][v] - flow[u][v] > 0) {
                        parent[v] = u;
                        queue.addLast(v);
                    }
                }
            }

            if (parent[sink] == -1) break;

            int augment = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                augment = Math.min(augment, capacity[u][v] - flow[u][v]);
            }
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += augment;
                flow[v][u] -= augment;
            }
            maxFlow += augment;
        }

        if (maxFlow != totalRequired) {
            return null;
        }

        int[] consumePerSlot = new int[slotCount];
        for (int i = 0; i < ingredientCount; i++) {
            for (int j = 0; j < slotCount; j++) {
                int used = flow[ingredientStart + i][slotStart + j];
                if (used > 0) consumePerSlot[j] += used;
            }
        }
        return consumePerSlot;
    }

    public static boolean canSatisfy(
            ItemStackHandler inventory,
            int startSlot,
            int endSlotExclusive,
            List<CountedIngredient> requirements
    ) {
        return find(inventory, startSlot, endSlotExclusive, requirements) != null;
    }
}
