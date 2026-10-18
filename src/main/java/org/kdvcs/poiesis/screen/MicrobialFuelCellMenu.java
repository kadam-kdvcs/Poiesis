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
import org.kdvcs.poiesis.block.entity.MicrobialFuelCellBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.MicrobialFuelCellGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;

public final class MicrobialFuelCellMenu extends AbstractMachineMenu {
    private static final int BATTERY_MENU_SLOT = 3;
    private static final int MACHINE_SLOT_COUNT = 4;
    public static final int DATA_COUNT = 21;

    private final MicrobialFuelCellBlockEntity blockEntity;

    public MicrobialFuelCellMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, requireBlockEntity(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT));
    }

    public MicrobialFuelCellMenu(
            int id,
            Inventory inventory,
            MicrobialFuelCellBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.MICROBIAL_FUEL_CELL.get(),
                id,
                inventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.MICROBIAL_FUEL_CELL.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(
                modules, ModuleComponent.PROCESS_SLOT,
                MicrobialFuelCellGuiLayout.SPECIALIZATION_X,
                MicrobialFuelCellGuiLayout.SPECIALIZATION_Y,
                ModuleKind.PROCESS
        ));
        addSlot(new ModuleSlot(
                modules, ModuleComponent.FIRST_PERFORMANCE_SLOT,
                MicrobialFuelCellGuiLayout.FIRST_PERFORMANCE_X,
                MicrobialFuelCellGuiLayout.FIRST_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        addSlot(new ModuleSlot(
                modules, ModuleComponent.SECOND_PERFORMANCE_SLOT,
                MicrobialFuelCellGuiLayout.SECOND_PERFORMANCE_X,
                MicrobialFuelCellGuiLayout.SECOND_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        blockEntity.runtime().battery().ifPresent(battery -> addSlot(new BatterySlot(
                battery.items(), 0,
                MicrobialFuelCellGuiLayout.BATTERY_X,
                MicrobialFuelCellGuiLayout.BATTERY_Y,
                BatterySlotComponent::isChargeableBatteryItem
        )));

        addPlayerInventory(
                MicrobialFuelCellGuiLayout.PLAYER_INVENTORY_X,
                MicrobialFuelCellGuiLayout.PLAYER_INVENTORY_Y,
                MicrobialFuelCellGuiLayout.PLAYER_HOTBAR_Y
        );
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem) return false;
        return BatterySlotComponent.isChargeableBatteryItem(stack)
                && moveToMachineSlot(stack, BATTERY_MENU_SLOT);
    }

    @Override public BlockPos machinePos() { return blockEntity.getBlockPos(); }
    @Override public boolean hasSideConfiguration() { return true; }

    public int progress() { return int32DataValue(0, 13); }
    public int maxProgress() { return int32DataValue(1, 14); }
    public int energy() { return int32DataValue(2, 15); }
    public int energyCapacity() { return int32DataValue(3, 16); }
    public int generationPerTick() { return dataValue(4); }
    public int outputPower() { return dataValue(5); }
    public int anolyteAmount() { return int32DataValue(6, 17); }
    public int anolyteCapacity() { return int32DataValue(7, 18); }
    public FluidStack anolyteStack() { return fluidStack(dataValue(8), anolyteAmount()); }
    public int catholyteAmount() { return int32DataValue(9, 19); }
    public int catholyteCapacity() { return int32DataValue(10, 20); }
    public FluidStack catholyteStack() { return fluidStack(dataValue(11), catholyteAmount()); }

    private static FluidStack fluidStack(int id, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.byId(id & 0xFFFF);
        return fluid == null || fluid == Fluids.EMPTY || amount <= 0
                ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static MicrobialFuelCellBlockEntity requireBlockEntity(
            Inventory inventory,
            BlockPos pos
    ) {
        if (inventory.player.level().getBlockEntity(pos)
                instanceof MicrobialFuelCellBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Microbial fuel cell is missing at " + pos);
    }
}
