package org.kdvcs.poiesis.screen.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleKind;

import java.util.function.Predicate;

/**
 * GUI slot for machine modules.
 *
 * The ModuleComponent/ModuleLayout is the source of truth for whether a
 * particular module is legal in a slot. The extra kind check only describes
 * the visible slot role; it must never bypass the handler validator during
 * shift-click quick moves.
 */
public final class ModuleSlot extends SlotItemHandler {

    private final ModuleKind acceptedKind;
    private final Predicate<ItemStack> additionalValidator;

    public ModuleSlot(
            IItemHandler itemHandler,
            int slot,
            int x,
            int y,
            ModuleKind acceptedKind
    ) {
        this(itemHandler, slot, x, y, acceptedKind, stack -> true);
    }

    public ModuleSlot(
            IItemHandler itemHandler,
            int slot,
            int x,
            int y,
            ModuleKind acceptedKind,
            Predicate<ItemStack> additionalValidator
    ) {
        super(itemHandler, slot, x, y);
        this.acceptedKind = acceptedKind;
        this.additionalValidator = additionalValidator;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return super.mayPlace(stack)
                && stack.getItem() instanceof MachineModuleItem module
                && module.kind() == acceptedKind
                && additionalValidator.test(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
