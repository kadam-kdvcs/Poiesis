package org.kdvcs.poiesis.screen.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleKind;

public final class ModuleSlot
        extends SlotItemHandler {

    private final ModuleKind acceptedKind;

    public ModuleSlot(
            IItemHandler itemHandler,
            int slot,
            int x,
            int y,
            ModuleKind acceptedKind
    ) {
        super(itemHandler, slot, x, y);
        this.acceptedKind = acceptedKind;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem()
                instanceof MachineModuleItem module
                && module.kind() == acceptedKind;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}