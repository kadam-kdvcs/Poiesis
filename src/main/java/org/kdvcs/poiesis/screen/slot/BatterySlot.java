package org.kdvcs.poiesis.screen.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;

public final class BatterySlot
        extends SlotItemHandler {

    public BatterySlot(
            IItemHandler itemHandler,
            int index,
            int x,
            int y
    ) {
        super(
                itemHandler,
                index,
                x,
                y
        );
    }

    @Override
    public boolean mayPlace(
            ItemStack stack
    ) {
        return BatterySlotComponent
                .isValidBatteryItem(stack);
    }
}