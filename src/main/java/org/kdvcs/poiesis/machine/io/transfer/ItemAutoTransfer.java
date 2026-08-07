package org.kdvcs.poiesis.machine.io.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

public final class ItemAutoTransfer {

    private static final int MAX_TRANSFER_PER_SIDE = 64;

    private ItemAutoTransfer() {
    }

    public static void pullInputs(
            MachineContext owner,
            MachineDefinition definition,
            InventoryComponent inventory,
            SideConfigurationComponent configuration
    ) {
        Level level = owner.level();

        if (level == null || level.isClientSide) {
            return;
        }

        for (RelativeSide relativeSide : RelativeSide.values()) {
            if (!configuration.allowsInput(IoChannel.ITEM, relativeSide)) {
                continue;
            }

            Direction worldSide = relativeSide.toWorldSide(owner.facing());
            IItemHandler neighbor = neighborItemHandler(level, owner.pos(), worldSide);

            if (neighbor == null) {
                continue;
            }

            pullFromNeighbor(
                    neighbor,
                    inventory.items(),
                    definition.inputSlots()
            );
        }
    }

    public static void pushOutputs(
            MachineContext owner,
            MachineDefinition definition,
            InventoryComponent inventory,
            SideConfigurationComponent configuration
    ) {
        Level level = owner.level();

        if (level == null || level.isClientSide) {
            return;
        }

        int firstOutputSlot = definition.inputSlots();
        int outputEnd = firstOutputSlot + definition.outputSlots();

        for (RelativeSide relativeSide : RelativeSide.values()) {
            if (!configuration.allowsOutput(IoChannel.ITEM, relativeSide)) {
                continue;
            }

            Direction worldSide = relativeSide.toWorldSide(owner.facing());
            IItemHandler neighbor = neighborItemHandler(level, owner.pos(), worldSide);

            if (neighbor == null) {
                continue;
            }

            pushToNeighbor(
                    inventory.items(),
                    firstOutputSlot,
                    outputEnd,
                    neighbor
            );
        }
    }

    private static IItemHandler neighborItemHandler(
            Level level,
            BlockPos machinePos,
            Direction worldSide
    ) {
        BlockPos neighborPos = machinePos.relative(worldSide);
        BlockEntity neighbor = level.getBlockEntity(neighborPos);

        if (neighbor == null) {
            return null;
        }

        return neighbor.getCapability(
                        ForgeCapabilities.ITEM_HANDLER,
                        worldSide.getOpposite()
                )
                .resolve()
                .orElse(null);
    }

    private static void pullFromNeighbor(
            IItemHandler source,
            IItemHandler destination,
            int inputSlots
    ) {
        int remainingBudget = MAX_TRANSFER_PER_SIDE;

        for (int sourceSlot = 0;
             sourceSlot < source.getSlots() && remainingBudget > 0;
             sourceSlot++) {

            ItemStack simulatedExtract = source.extractItem(
                    sourceSlot,
                    remainingBudget,
                    true
            );

            if (simulatedExtract.isEmpty()) {
                continue;
            }

            int accepted = acceptedByInputs(
                    destination,
                    inputSlots,
                    simulatedExtract
            );

            if (accepted <= 0) {
                continue;
            }

            ItemStack extracted = source.extractItem(
                    sourceSlot,
                    accepted,
                    false
            );

            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = insertIntoInputs(
                    destination,
                    inputSlots,
                    extracted,
                    false
            );

            int moved = extracted.getCount() - remainder.getCount();
            remainingBudget -= Math.max(0, moved);

            if (!remainder.isEmpty()) {
                returnRemainder(source, sourceSlot, remainder);
            }
        }
    }

    private static void pushToNeighbor(
            IItemHandler source,
            int firstOutputSlot,
            int outputEnd,
            IItemHandler destination
    ) {
        int remainingBudget = MAX_TRANSFER_PER_SIDE;

        for (int sourceSlot = firstOutputSlot;
             sourceSlot < outputEnd && remainingBudget > 0;
             sourceSlot++) {

            ItemStack simulatedExtract = source.extractItem(
                    sourceSlot,
                    remainingBudget,
                    true
            );

            if (simulatedExtract.isEmpty()) {
                continue;
            }

            ItemStack simulatedRemainder = insertIntoAllSlots(
                    destination,
                    simulatedExtract,
                    true
            );

            int accepted = simulatedExtract.getCount()
                    - simulatedRemainder.getCount();

            if (accepted <= 0) {
                continue;
            }

            ItemStack extracted = source.extractItem(
                    sourceSlot,
                    accepted,
                    false
            );

            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = insertIntoAllSlots(
                    destination,
                    extracted,
                    false
            );

            int moved = extracted.getCount() - remainder.getCount();
            remainingBudget -= Math.max(0, moved);

            if (!remainder.isEmpty()) {
                restoreOutputStack(source, sourceSlot, remainder);
            }
        }
    }

    private static int acceptedByInputs(
            IItemHandler destination,
            int inputSlots,
            ItemStack stack
    ) {
        ItemStack remainder = insertIntoInputs(
                destination,
                inputSlots,
                stack,
                true
        );

        return stack.getCount() - remainder.getCount();
    }

    private static ItemStack insertIntoInputs(
            IItemHandler destination,
            int inputSlots,
            ItemStack stack,
            boolean simulate
    ) {
        ItemStack remainder = stack.copy();

        for (int slot = 0;
             slot < inputSlots && !remainder.isEmpty();
             slot++) {
            remainder = destination.insertItem(
                    slot,
                    remainder,
                    simulate
            );
        }

        return remainder;
    }

    private static ItemStack insertIntoAllSlots(
            IItemHandler destination,
            ItemStack stack,
            boolean simulate
    ) {
        ItemStack remainder = stack.copy();

        for (int slot = 0;
             slot < destination.getSlots() && !remainder.isEmpty();
             slot++) {
            remainder = destination.insertItem(
                    slot,
                    remainder,
                    simulate
            );
        }

        return remainder;
    }

    private static void returnRemainder(
            IItemHandler source,
            int preferredSlot,
            ItemStack remainder
    ) {
        ItemStack left = source.insertItem(
                preferredSlot,
                remainder,
                false
        );

        if (left.isEmpty()) {
            return;
        }

        insertIntoAllSlots(source, left, false);
    }

    private static void restoreOutputStack(
            IItemHandler source,
            int sourceSlot,
            ItemStack remainder
    ) {
        if (source instanceof net.minecraftforge.items.ItemStackHandler handler) {
            ItemStack current = handler.getStackInSlot(sourceSlot);

            if (current.isEmpty()) {
                handler.setStackInSlot(sourceSlot, remainder);
                return;
            }

            if (ItemStack.isSameItemSameTags(current, remainder)) {
                ItemStack restored = current.copy();
                restored.grow(remainder.getCount());
                handler.setStackInSlot(sourceSlot, restored);
            }
        }
    }
}
