package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class MechanicalProcessorMenu
        extends AbstractMachineMenu {

    private static final int INPUT_MENU_SLOT = 0;
    private static final int PROCESS_MODULE_MENU_SLOT = 2;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 3;
    private static final int PERFORMANCE_MENU_SLOT_END = 5;

    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int DATA_COUNT = 8;

    private static final int DATA_PROGRESS = 0;
    private static final int DATA_MAX_PROGRESS = 1;
    private static final int DATA_ENERGY = 2;
    private static final int DATA_CAPACITY = 3;
    private static final int DATA_ENERGY_PER_TICK = 4;
    private static final int DATA_VOLTAGE = 5;
    private static final int DATA_STATE = 6;
    private static final int DATA_MODE = 7;

    private final MechanicalProcessorBlockEntity blockEntity;

    public MechanicalProcessorMenu(
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

    public MechanicalProcessorMenu(
            int containerId,
            Inventory playerInventory,
            MechanicalProcessorBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.MECHANICAL_PROCESSOR.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                ),
                ModBlocks.MECHANICAL_PROCESSOR.get(),
                MACHINE_SLOT_COUNT
        );

        this.blockEntity = blockEntity;

        checkContainerDataCount(data, DATA_COUNT);

        addMachineSlots();
        addPlayerInventory(8, 90);
        addDataSlots(data);
    }

    private void addMachineSlots() {
        addSlot(
                new SlotItemHandler(
                        blockEntity.runtime()
                                .inventory()
                                .items(),
                        0,
                        44,
                        34
                )
        );

        addSlot(
                new OutputSlot(
                        blockEntity.runtime()
                                .inventory()
                                .items(),
                        1,
                        116,
                        34
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.PROCESS_SLOT,
                        152,
                        16,
                        ModuleKind.PROCESS
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.FIRST_PERFORMANCE_SLOT,
                        152,
                        34,
                        ModuleKind.PERFORMANCE
                )
        );

        addSlot(
                new ModuleSlot(
                        blockEntity.runtime()
                                .modules()
                                .items(),
                        ModuleComponent.SECOND_PERFORMANCE_SLOT,
                        152,
                        52,
                        ModuleKind.PERFORMANCE
                )
        );
    }

    @Override
    protected boolean movePlayerStackToMachine(
            ItemStack stack
    ) {
        if (stack.getItem()
                instanceof MachineModuleItem module) {
            return switch (module.kind()) {
                case PROCESS -> moveToMachineSlot(
                        stack,
                        PROCESS_MODULE_MENU_SLOT
                );

                case PERFORMANCE -> moveToMachineSlots(
                        stack,
                        FIRST_PERFORMANCE_MENU_SLOT,
                        PERFORMANCE_MENU_SLOT_END
                );

                case PROTECTION -> false;
            };
        }

        return moveToMachineSlot(
                stack,
                INPUT_MENU_SLOT
        );
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return blockEntity.machineDefinition()
                .io()
                .isEmpty() == false;
    }

    public int progress() {
        return dataValue(DATA_PROGRESS);
    }

    public int maxProgress() {
        return dataValue(DATA_MAX_PROGRESS);
    }

    public int energy() {
        return dataValue(DATA_ENERGY);
    }

    public int capacity() {
        return dataValue(DATA_CAPACITY);
    }

    public int activeEnergyPerTick() {
        return dataValue(DATA_ENERGY_PER_TICK);
    }

    public int activeVoltage() {
        return dataValue(DATA_VOLTAGE);
    }

    public int stateOrdinal() {
        return dataValue(DATA_STATE);
    }

    public MechanicalMode mode() {
        return enumValue(
                DATA_MODE,
                MechanicalMode.values(),
                MechanicalMode.NONE
        );
    }

    private static MechanicalProcessorBlockEntity
    requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player
                .level()
                .getBlockEntity(pos)
                instanceof MechanicalProcessorBlockEntity blockEntity) {
            return blockEntity;
        }

        throw new IllegalStateException(
                "Mechanical processor block entity is missing at "
                        + pos
        );
    }
}