package org.kdvcs.poiesis.machine.separator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;

/** Dedicated, non-consumable process equipment slot for electrostatic separation. */
public final class ElectrodeComponent implements MachineComponent {
    private final TagKey<Item> acceptedTag;
    private final ItemStackHandler items;
    private long version;

    public ElectrodeComponent(TagKey<Item> acceptedTag, Runnable changed) {
        this.acceptedTag = acceptedTag;
        this.items = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == 0 && stack.is(ElectrodeComponent.this.acceptedTag);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                version++;
                changed.run();
            }
        };
    }

    @Override
    public String key() {
        return "electrode";
    }

    public ItemStackHandler items() {
        return items;
    }

    public boolean hasElectrode() {
        ItemStack stack = items.getStackInSlot(0);
        return !stack.isEmpty() && stack.is(acceptedTag);
    }

    public boolean isEmpty() {
        return items.getStackInSlot(0).isEmpty();
    }

    public long version() {
        return version;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("item", items.serializeNBT());
        tag.putLong("version", version);
    }

    @Override
    public void load(CompoundTag tag) {
        items.deserializeNBT(tag.getCompound("item"));
        version = tag.getLong("version");
    }
}
