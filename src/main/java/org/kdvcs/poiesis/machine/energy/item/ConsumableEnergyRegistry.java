package org.kdvcs.poiesis.machine.energy.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ConsumableEnergyRegistry {

    private static final Map<Item, Integer> VALUES =
            new IdentityHashMap<>();

    private ConsumableEnergyRegistry() {
    }

    public static void register(
            ItemLike item,
            int energy
    ) {
        if (energy <= 0) {
            throw new IllegalArgumentException(
                    "Consumable energy value must be positive"
            );
        }

        VALUES.put(
                item.asItem(),
                energy
        );
    }

    public static int energyValue(
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return 0;
        }

        return VALUES.getOrDefault(
                stack.getItem(),
                0
        );
    }

    public static boolean contains(
            ItemStack stack
    ) {
        return energyValue(stack) > 0;
    }
}