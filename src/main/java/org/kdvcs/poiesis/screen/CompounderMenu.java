package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.CompounderBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.CompounderGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class CompounderMenu
        extends AbstractMachineMenu {

    private static final int FIRST_INPUT_MENU_SLOT = 0;
    private static final int INPUT_MENU_SLOT_END = 2;
    private static final int OUTPUT_MENU_SLOT = 2;

    private static final int SPECIALIZATION_MODULE_MENU_SLOT = 3;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 4;
    private static final int PERFORMANCE_MENU_SLOT_END = 6;

    private static final int BATTERY_MENU_SLOT = 6;

    private static final int MACHINE_SLOT_COUNT = 7;
    private static final int DATA_COUNT = 7;


    private final CompounderBlockEntity blockEntity;

    public CompounderMenu(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                requireBlockEntity(
                        playerInventory,
                        buffer.readBlockPos()
                ),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public CompounderMenu(
            int containerId,
            Inventory playerInventory,
            CompounderBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.COMPOUNDER.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                ),
                ModBlocks.COMPOUNDER.get(),
                MACHINE_SLOT_COUNT
        );

        this.blockEntity = blockEntity;

        checkContainerDataCount(
                data,
                DATA_COUNT
        );

        addMachineSlots();

        addPlayerInventory(
                8,
                90
        );

        addDataSlots(data);
    }

    private void addMachineSlots() {
        var inventory =
                blockEntity.runtime()
                        .inventory()
                        .items();

        addSlot(
                new SlotItemHandler(
                        inventory,
                        0,
                        CompounderGuiLayout.FIRST_INPUT_X,
                        CompounderGuiLayout.FIRST_INPUT_Y
                )
        );

        addSlot(
                new SlotItemHandler(
                        inventory,
                        0 + 1,
                        CompounderGuiLayout.SECOND_INPUT_X,
                        CompounderGuiLayout.SECOND_INPUT_Y
                )
        );

        addSlot(
                new OutputSlot(
                        inventory,
                        CompounderBlockEntity.DEFINITION.inputSlots(),
                        CompounderGuiLayout.OUTPUT_X,
                        CompounderGuiLayout.OUTPUT_Y
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.PROCESS_SLOT,
                        CompounderGuiLayout.SPECIALIZATION_X,
                        CompounderGuiLayout.SPECIALIZATION_Y,
                        ModuleKind.PROCESS
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.FIRST_PERFORMANCE_SLOT,
                        CompounderGuiLayout.FIRST_PERFORMANCE_X,
                        CompounderGuiLayout.FIRST_PERFORMANCE_Y,
                        ModuleKind.PERFORMANCE
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.SECOND_PERFORMANCE_SLOT,
                        CompounderGuiLayout.SECOND_PERFORMANCE_X,
                        CompounderGuiLayout.SECOND_PERFORMANCE_Y,
                        ModuleKind.PERFORMANCE
                )
        );

        blockEntity.runtime()
                .battery()
                .ifPresent(
                        battery ->
                                addSlot(
                                        new BatterySlot(
                                                battery.items(),
                                                0,
                                                CompounderGuiLayout.BATTERY_X,
                                                CompounderGuiLayout.BATTERY_Y
                                        )
                                )
                );
    }

    @Override
    protected boolean movePlayerStackToMachine(
            ItemStack stack
    ) {
        if (stack.getItem()
                instanceof MachineModuleItem module) {

            if (module.kind() == ModuleKind.PROCESS) {
                var moduleInventory =
                        blockEntity.runtime()
                                .modules()
                                .items();

                if (!moduleInventory.isItemValid(
                        ModuleComponent.PROCESS_SLOT,
                        stack
                )) {
                    return false;
                }

                return moveToMachineSlot(
                        stack,
                        SPECIALIZATION_MODULE_MENU_SLOT
                );
            }

            if (module.kind() == ModuleKind.PERFORMANCE) {
                return moveToMachineSlots(
                        stack,
                        FIRST_PERFORMANCE_MENU_SLOT,
                        PERFORMANCE_MENU_SLOT_END
                );
            }
        }

        if (BatterySlotComponent.isValidBatteryItem(stack)) {
            if (moveToMachineSlot(
                    stack,
                    BATTERY_MENU_SLOT
            )) {
                return true;
            }
        }

        return moveToMachineSlots(
                stack,
                FIRST_INPUT_MENU_SLOT,
                INPUT_MENU_SLOT_END
        );
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return !blockEntity.machineDefinition()
                .io()
                .isEmpty();
    }

    public int progress() {
        return dataValue(0);
    }

    public int maxProgress() {
        return dataValue(1);
    }

    public int energy() {
        return dataValue(2);
    }

    public int capacity() {
        return dataValue(3);
    }

    public int activeEnergyPerTick() {
        return dataValue(4);
    }

    public int activeVoltage() {
        return dataValue(5);
    }

    public int stateOrdinal() {
        return dataValue(6);
    }

    private static CompounderBlockEntity requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player
                .level()
                .getBlockEntity(pos)
                instanceof CompounderBlockEntity blockEntity) {

            return blockEntity;
        }

        throw new IllegalStateException(
                "Compounder block entity is missing at "
                        + pos
        );
    }
}
