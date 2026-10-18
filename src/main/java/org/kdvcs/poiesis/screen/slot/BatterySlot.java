package org.kdvcs.poiesis.screen.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;

import java.util.function.Predicate;

public final class BatterySlot
        extends SlotItemHandler {

    public BatterySlot(
            IItemHandler itemHandler,
            int index,
            int x,
            int y
    ) {
        this(itemHandler, index, x, y, BatterySlotComponent::isValidBatteryItem);
    }

    private final Predicate<ItemStack> validator;

    public BatterySlot(
            IItemHandler itemHandler,
            int index,
            int x,
            int y,
            Predicate<ItemStack> validator
    ) {
        super(
                itemHandler,
                index,
                x,
                y
        );
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(
            ItemStack stack
    ) {
        return validator.test(stack);
    }
}
