package org.kdvcs.poiesis.screen.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class OutputSlot
        extends SlotItemHandler {

    public OutputSlot(
            IItemHandler itemHandler,
            int slot,
            int x,
            int y
    ) {
        super(itemHandler, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}