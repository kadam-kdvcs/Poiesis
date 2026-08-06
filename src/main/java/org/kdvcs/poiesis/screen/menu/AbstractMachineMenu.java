package org.kdvcs.poiesis.screen.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMachineMenu
        extends AbstractContainerMenu {

    public static final int DEFAULT_SLOT_SPACING = 18;
    public static final int DEFAULT_HOTBAR_OFFSET = 58;

    protected final Inventory playerInventory;
    protected final ContainerData data;
    protected final ContainerLevelAccess access;
    protected final Block validBlock;

    private final int machineSlotCount;

    protected AbstractMachineMenu(
            @Nullable MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            ContainerData data,
            ContainerLevelAccess access,
            Block validBlock,
            int machineSlotCount
    ) {
        super(menuType, containerId);

        this.playerInventory = playerInventory;
        this.data = data;
        this.access = access;
        this.validBlock = validBlock;
        this.machineSlotCount = machineSlotCount;
    }

    protected final void addPlayerInventory(
            int startX,
            int startY
    ) {
        addPlayerInventory(
                startX,
                startY,
                startY + DEFAULT_HOTBAR_OFFSET
        );
    }

    protected final void addPlayerInventory(
            int startX,
            int inventoryStartY,
            int hotbarY
    ) {
        addPlayerMainInventory(
                startX,
                inventoryStartY
        );

        addPlayerHotbar(
                startX,
                hotbarY
        );
    }

    protected final void addPlayerMainInventory(
            int startX,
            int startY
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventoryIndex =
                        column + row * 9 + 9;

                int x =
                        startX
                                + column
                                * DEFAULT_SLOT_SPACING;

                int y =
                        startY
                                + row
                                * DEFAULT_SLOT_SPACING;

                addSlot(
                        new Slot(
                                playerInventory,
                                inventoryIndex,
                                x,
                                y
                        )
                );
            }
        }
    }

    protected final void addPlayerHotbar(
            int startX,
            int y
    ) {
        for (int column = 0; column < 9; column++) {
            int x =
                    startX
                            + column
                            * DEFAULT_SLOT_SPACING;

            addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            x,
                            y
                    )
            );
        }
    }

    @Override
    public final ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack =
                sourceSlot.getItem();

        ItemStack originalStack =
                sourceStack.copy();

        boolean moved;

        if (isMachineSlot(index)) {
            moved = moveMachineStackToPlayer(
                    sourceStack
            );
        } else {
            moved = movePlayerStackToMachine(
                    sourceStack
            );
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount()
                == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);

        return originalStack;
    }

    protected boolean moveMachineStackToPlayer(
            ItemStack stack
    ) {
        return moveItemStackTo(
                stack,
                machineSlotCount,
                slots.size(),
                true
        );
    }

    protected abstract boolean movePlayerStackToMachine(
            ItemStack stack
    );

    protected final boolean moveToMachineSlot(
            ItemStack stack,
            int slotIndex
    ) {
        return moveItemStackTo(
                stack,
                slotIndex,
                slotIndex + 1,
                false
        );
    }

    protected final boolean moveToMachineSlots(
            ItemStack stack,
            int startInclusive,
            int endExclusive
    ) {
        return moveItemStackTo(
                stack,
                startInclusive,
                endExclusive,
                false
        );
    }

    protected final boolean isMachineSlot(
            int menuSlotIndex
    ) {
        return menuSlotIndex < machineSlotCount;
    }

    protected final int machineSlotCount() {
        return machineSlotCount;
    }

    protected final int dataValue(int index) {
        return data.get(index);
    }

    protected final <E extends Enum<E>> E enumValue(
            int dataIndex,
            E[] values,
            E fallback
    ) {
        int ordinal = data.get(dataIndex);

        if (ordinal < 0 || ordinal >= values.length) {
            return fallback;
        }

        return values[ordinal];
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                access,
                player,
                validBlock
        );
    }
}