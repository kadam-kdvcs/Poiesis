package org.kdvcs.poiesis.machine.io.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

import java.util.function.Supplier;

public final class RoutedItemHandler implements IItemHandler {
    private final IItemHandler delegate;
    private final SideConfigurationComponent configuration;
    private final Supplier<RelativeSide> sideSupplier;
    private final int inputSlots;
    private final int outputSlots;

    public RoutedItemHandler(
            IItemHandler delegate,
            SideConfigurationComponent configuration,
            Supplier<RelativeSide> sideSupplier,
            int inputSlots,
            int outputSlots
    ) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.sideSupplier = sideSupplier;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    @Override
    public int getSlots() {
        return inputSlots + outputSlots;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return isValidSlot(slot)
                ? delegate.getStackInSlot(slot)
                : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(
            int slot,
            @NotNull ItemStack stack,
            boolean simulate
    ) {
        if (!isInputSlot(slot)
                || !configuration.allowsInput(IoChannel.ITEM, sideSupplier.get())) {
            return stack;
        }

        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(
            int slot,
            int amount,
            boolean simulate
    ) {
        if (!isOutputSlot(slot)
                || !configuration.allowsOutput(IoChannel.ITEM, sideSupplier.get())) {
            return ItemStack.EMPTY;
        }

        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isValidSlot(slot)
                ? delegate.getSlotLimit(slot)
                : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isInputSlot(slot)
                && configuration.allowsInput(IoChannel.ITEM, sideSupplier.get())
                && delegate.isItemValid(slot, stack);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < getSlots();
    }

    private boolean isInputSlot(int slot) {
        return slot >= 0 && slot < inputSlots;
    }

    private boolean isOutputSlot(int slot) {
        return slot >= inputSlots
                && slot < inputSlots + outputSlots;
    }
}
