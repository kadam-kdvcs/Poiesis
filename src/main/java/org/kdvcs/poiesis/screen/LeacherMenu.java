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
import org.kdvcs.poiesis.block.entity.LeacherBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.LeacherGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class LeacherMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 19;
    private static final int INPUT_MENU_SLOT = 0;
    private static final int PROCESS_MODULE_MENU_SLOT = 2;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 3;
    private static final int PERFORMANCE_MENU_SLOT_END = 5;
    private static final int BATTERY_MENU_SLOT = 5;
    private static final int MACHINE_SLOT_COUNT = 6;

    private final LeacherBlockEntity blockEntity;

    public LeacherMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, requireBlockEntity(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT));
    }

    public LeacherMenu(
            int id,
            Inventory playerInventory,
            LeacherBlockEntity blockEntity,
            ContainerData data
    ) {
        super(ModMenuTypes.LEACHER.get(), id, playerInventory, data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.LEACHER.get(), MACHINE_SLOT_COUNT);
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var items = blockEntity.runtime().inventory().items();
        addSlot(new SlotItemHandler(items, 0,
                LeacherGuiLayout.ITEM_INPUT_X, LeacherGuiLayout.ITEM_INPUT_Y));
        addSlot(new OutputSlot(items, 1,
                LeacherGuiLayout.ITEM_OUTPUT_X, LeacherGuiLayout.ITEM_OUTPUT_Y));

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(modules, ModuleComponent.PROCESS_SLOT,
                LeacherGuiLayout.PROCESS_MODULE_X, LeacherGuiLayout.PROCESS_MODULE_Y,
                ModuleKind.PROCESS));
        addSlot(new ModuleSlot(modules, ModuleComponent.FIRST_PERFORMANCE_SLOT,
                LeacherGuiLayout.PERFORMANCE_ONE_X, LeacherGuiLayout.PERFORMANCE_ONE_Y,
                ModuleKind.PERFORMANCE));
        addSlot(new ModuleSlot(modules, ModuleComponent.SECOND_PERFORMANCE_SLOT,
                LeacherGuiLayout.PERFORMANCE_TWO_X, LeacherGuiLayout.PERFORMANCE_TWO_Y,
                ModuleKind.PERFORMANCE));

        blockEntity.runtime().battery().ifPresent(battery -> addSlot(new BatterySlot(
                battery.items(), 0, LeacherGuiLayout.BATTERY_X, LeacherGuiLayout.BATTERY_Y
        )));
        addPlayerInventory(LeacherGuiLayout.PLAYER_INVENTORY_X,
                LeacherGuiLayout.PLAYER_INVENTORY_Y);
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem module) {
            return switch (module.kind()) {
                case PROCESS -> moveToMachineSlot(stack, PROCESS_MODULE_MENU_SLOT);
                case PERFORMANCE -> moveToMachineSlots(
                        stack, FIRST_PERFORMANCE_MENU_SLOT, PERFORMANCE_MENU_SLOT_END);
                case PROTECTION -> false;
            };
        }
        if (BatterySlotComponent.isValidBatteryItem(stack)) {
            return moveToMachineSlot(stack, BATTERY_MENU_SLOT);
        }
        return moveToMachineSlot(stack, INPUT_MENU_SLOT);
    }

    @Override public BlockPos machinePos() { return blockEntity.getBlockPos(); }
    @Override public boolean hasSideConfiguration() { return true; }

    public int progress() { return dataValue(0); }
    public int maxProgress() { return dataValue(1); }
    public int energy() { return int32DataValue(2, 13); }
    public int energyCapacity() { return int32DataValue(3, 14); }
    public int activeEnergyPerTick() { return dataValue(4); }
    public int stateOrdinal() { return dataValue(6); }
    public int inputFluidAmount() { return int32DataValue(7, 15); }
    public int inputFluidCapacity() { return int32DataValue(8, 16); }
    public int outputFluidAmount() { return int32DataValue(10, 17); }
    public int outputFluidCapacity() { return int32DataValue(11, 18); }
    public FluidStack inputFluidStack() { return fluidStack(dataValue(9), inputFluidAmount()); }
    public FluidStack outputFluidStack() { return fluidStack(dataValue(12), outputFluidAmount()); }

    private static FluidStack fluidStack(int registryId, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.byId(registryId & 0xFFFF);
        return fluid == null || fluid == Fluids.EMPTY || amount <= 0
                ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static LeacherBlockEntity requireBlockEntity(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof LeacherBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Leacher block entity is missing at " + pos);
    }
}
