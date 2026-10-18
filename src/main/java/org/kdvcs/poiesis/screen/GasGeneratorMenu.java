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
import org.kdvcs.poiesis.block.entity.GasGeneratorBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.GasGeneratorGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;

public final class GasGeneratorMenu extends AbstractMachineMenu {
    private static final int BATTERY_MENU_SLOT = 3;
    private static final int MACHINE_SLOT_COUNT = 4;
    public static final int DATA_COUNT = 16;

    private final GasGeneratorBlockEntity blockEntity;

    public GasGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                containerId,
                playerInventory,
                requireBlockEntity(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public GasGeneratorMenu(
            int containerId,
            Inventory playerInventory,
            GasGeneratorBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.GAS_GENERATOR.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.GAS_GENERATOR.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.PROCESS_SLOT,
                GasGeneratorGuiLayout.SPECIALIZATION_X,
                GasGeneratorGuiLayout.SPECIALIZATION_Y,
                ModuleKind.PROCESS
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.FIRST_PERFORMANCE_SLOT,
                GasGeneratorGuiLayout.FIRST_PERFORMANCE_X,
                GasGeneratorGuiLayout.FIRST_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.SECOND_PERFORMANCE_SLOT,
                GasGeneratorGuiLayout.SECOND_PERFORMANCE_X,
                GasGeneratorGuiLayout.SECOND_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        blockEntity.runtime().battery().ifPresent(battery -> addSlot(new BatterySlot(
                battery.items(),
                0,
                GasGeneratorGuiLayout.BATTERY_X,
                GasGeneratorGuiLayout.BATTERY_Y,
                BatterySlotComponent::isChargeableBatteryItem
        )));

        addPlayerInventory(
                GasGeneratorGuiLayout.PLAYER_INVENTORY_X,
                GasGeneratorGuiLayout.PLAYER_INVENTORY_Y
        );
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem) {
            return false;
        }
        return BatterySlotComponent.isChargeableBatteryItem(stack)
                && moveToMachineSlot(stack, BATTERY_MENU_SLOT);
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return true;
    }

    public int burnRemaining() { return int32DataValue(0, 10); }
    public int burnTotal() { return int32DataValue(1, 11); }
    public int energy() { return int32DataValue(2, 12); }
    public int capacity() { return int32DataValue(3, 13); }
    public int generationPerTick() { return dataValue(4); }
    public int outputPower() { return dataValue(5); }
    public int fluidAmount() { return int32DataValue(6, 14); }
    public int fluidCapacity() { return int32DataValue(7, 15); }
    public int stateOrdinal() { return dataValue(9); }

    public FluidStack fluidStack() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(unsignedDataValue(8));
        if (fluid == null || fluid == Fluids.EMPTY || fluidAmount() <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, fluidAmount());
    }

    private static GasGeneratorBlockEntity requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player.level().getBlockEntity(pos)
                instanceof GasGeneratorBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Gas generator is missing at " + pos);
    }
}
