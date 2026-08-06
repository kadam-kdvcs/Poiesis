package org.kdvcs.poiesis.machine.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.function.IntPredicate;

public final class InventoryComponent
        implements MachineComponent {

    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> capability;
    private final IntPredicate outputSlot;
    private long version;

    public InventoryComponent(
            int size,
            IntPredicate outputSlot,
            Runnable changed
    ) {
        this.outputSlot = outputSlot;

        this.items = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                version++;
                changed.run();
            }

            @Override
            public boolean isItemValid(
                    int slot,
                    ItemStack stack
            ) {
                return !outputSlot.test(slot);
            }
        };

        this.capability =
                LazyOptional.of(() -> items);
    }

    @Override
    public String key() {
        return "inventory";
    }

    public ItemStackHandler items() {
        return items;
    }

    public LazyOptional<IItemHandler> capability() {
        return capability;
    }

    public long version() {
        return version;
    }

    public boolean isOutput(int slot) {
        return outputSlot.test(slot);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("items", items.serializeNBT());
        tag.putLong("version", version);
    }

    @Override
    public void load(CompoundTag tag) {
        items.deserializeNBT(tag.getCompound("items"));
        version = tag.getLong("version");
    }

    @Override
    public void invalidate() {
        capability.invalidate();
    }
}
