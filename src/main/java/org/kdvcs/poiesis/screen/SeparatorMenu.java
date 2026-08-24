package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.SeparatorBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.SeparatorGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class SeparatorMenu extends AbstractMachineMenu {
    private static final int FIRST_INPUT_MENU_SLOT = 0;
    private static final int INPUT_MENU_SLOT_END = 2;
    private static final int OUTPUT_MENU_SLOT_START = 2;
    private static final int OUTPUT_MENU_SLOT_END = 6;
    private static final int SPECIALIZATION_MODULE_MENU_SLOT = 6;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 7;
    private static final int PERFORMANCE_MENU_SLOT_END = 9;
    private static final int BATTERY_MENU_SLOT = 9;
    private static final int MACHINE_SLOT_COUNT = 10;

    public static final int DATA_COUNT = 11;

    private final SeparatorBlockEntity blockEntity;

    public SeparatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                requireBlockEntity(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public SeparatorMenu(
            int containerId,
            Inventory playerInventory,
            SeparatorBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.SEPARATOR.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.SEPARATOR.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);
        addMachineSlots();
        addPlayerInventory(8, 90);
        addDataSlots(data);
    }

    private void addMachineSlots() {
        var inventory = blockEntity.runtime().inventory().items();

        addSlot(new SlotItemHandler(
                inventory,
                0,
                SeparatorGuiLayout.FIRST_INPUT_X,
                SeparatorGuiLayout.FIRST_INPUT_Y
        ));
        addSlot(new SlotItemHandler(
                inventory,
                1,
                SeparatorGuiLayout.SECOND_INPUT_X,
                SeparatorGuiLayout.SECOND_INPUT_Y
        ));

        for (int index = 0; index < SeparatorBlockEntity.DEFINITION.outputSlots(); index++) {
            addSlot(new OutputSlot(
                    inventory,
                    SeparatorBlockEntity.DEFINITION.inputSlots() + index,
                    SeparatorGuiLayout.OUTPUT_X[index],
                    SeparatorGuiLayout.OUTPUT_Y[index]
            ));
        }

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.PROCESS_SLOT,
                SeparatorGuiLayout.SPECIALIZATION_X,
                SeparatorGuiLayout.SPECIALIZATION_Y,
                ModuleKind.PROCESS
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.FIRST_PERFORMANCE_SLOT,
                SeparatorGuiLayout.FIRST_PERFORMANCE_X,
                SeparatorGuiLayout.FIRST_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.SECOND_PERFORMANCE_SLOT,
                SeparatorGuiLayout.SECOND_PERFORMANCE_X,
                SeparatorGuiLayout.SECOND_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));

        blockEntity.runtime().battery().ifPresent(battery -> addSlot(
                new BatterySlot(
                        battery.items(),
                        0,
                        SeparatorGuiLayout.BATTERY_X,
                        SeparatorGuiLayout.BATTERY_Y
                )
        ));
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem module) {
            if (module.kind() == ModuleKind.PROCESS) {
                var modules = blockEntity.runtime().modules().items();
                if (!modules.isItemValid(ModuleComponent.PROCESS_SLOT, stack)) {
                    return false;
                }
                return moveToMachineSlot(stack, SPECIALIZATION_MODULE_MENU_SLOT);
            }

            if (module.kind() == ModuleKind.PERFORMANCE) {
                return moveToMachineSlots(
                        stack,
                        FIRST_PERFORMANCE_MENU_SLOT,
                        PERFORMANCE_MENU_SLOT_END
                );
            }
        }

        if (BatterySlotComponent.isValidBatteryItem(stack)
                && moveToMachineSlot(stack, BATTERY_MENU_SLOT)) {
            return true;
        }

        return moveToMachineSlots(stack, FIRST_INPUT_MENU_SLOT, INPUT_MENU_SLOT_END);
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return true;
    }

    public int progress() { return dataValue(0); }
    public int maxProgress() { return dataValue(1); }
    public int energy() { return dataValue(2) & 0xFFFF; }
    public int capacity() { return dataValue(3) & 0xFFFF; }
    public int activeEnergyPerTick() { return dataValue(4); }
    public int activeVoltage() { return dataValue(5); }
    public int stateOrdinal() { return dataValue(6); }
    public int fluidAmount() { return dataValue(7) & 0xFFFF; }
    public int fluidCapacity() { return dataValue(8) & 0xFFFF; }
    public boolean fluidSlotEnabled() { return dataValue(10) != 0; }

    public FluidStack fluidStack() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(dataValue(9) & 0xFFFF);
        if (fluid == null || fluid == Fluids.EMPTY || fluidAmount() <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, fluidAmount());
    }

    private static SeparatorBlockEntity requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player.level().getBlockEntity(pos)
                instanceof SeparatorBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Separator block entity is missing at " + pos);
    }
}
