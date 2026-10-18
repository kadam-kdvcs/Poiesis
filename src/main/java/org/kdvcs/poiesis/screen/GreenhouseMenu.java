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
import org.kdvcs.poiesis.block.entity.GreenhouseBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.GreenhouseGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class GreenhouseMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 23;

    private static final int FIRST_INPUT_MENU_SLOT = 0;
    private static final int INPUT_MENU_SLOT_END = 5;
    private static final int PROCESS_MODULE_MENU_SLOT = 7;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 8;
    private static final int PERFORMANCE_MENU_SLOT_END = 10;
    private static final int BATTERY_MENU_SLOT = 10;
    private static final int MACHINE_SLOT_COUNT = 11;

    private final GreenhouseBlockEntity blockEntity;

    public GreenhouseMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                id,
                inventory,
                requireBlockEntity(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public GreenhouseMenu(
            int id,
            Inventory playerInventory,
            GreenhouseBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.GREENHOUSE.get(),
                id,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.GREENHOUSE.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var items = blockEntity.runtime().inventory().items();
        for (int row = 0; row < GreenhouseGuiLayout.INPUT_ROWS; row++) {
            for (int column = 0; column < GreenhouseGuiLayout.INPUT_COLUMNS; column++) {
                int slot = row * GreenhouseGuiLayout.INPUT_COLUMNS + column;
                addSlot(new SlotItemHandler(
                        items,
                        slot,
                        GreenhouseGuiLayout.INPUT_X + column * GreenhouseGuiLayout.SLOT_SIZE,
                        GreenhouseGuiLayout.INPUT_Y + row * GreenhouseGuiLayout.SLOT_SIZE
                ));
            }
        }
        addSlot(new SlotItemHandler(
                items, 4, GreenhouseGuiLayout.NUTRIENT_ITEM_X, GreenhouseGuiLayout.NUTRIENT_ITEM_Y
        ));
        addSlot(new OutputSlot(
                items, 5, GreenhouseGuiLayout.FIRST_OUTPUT_X, GreenhouseGuiLayout.FIRST_OUTPUT_Y
        ));
        addSlot(new OutputSlot(
                items, 6, GreenhouseGuiLayout.SECOND_OUTPUT_X, GreenhouseGuiLayout.SECOND_OUTPUT_Y
        ));

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.PROCESS_SLOT,
                GreenhouseGuiLayout.PROCESS_MODULE_X,
                GreenhouseGuiLayout.PROCESS_MODULE_Y,
                ModuleKind.PROCESS
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.FIRST_PERFORMANCE_SLOT,
                GreenhouseGuiLayout.FIRST_PERFORMANCE_X,
                GreenhouseGuiLayout.FIRST_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        addSlot(new ModuleSlot(
                modules,
                ModuleComponent.SECOND_PERFORMANCE_SLOT,
                GreenhouseGuiLayout.SECOND_PERFORMANCE_X,
                GreenhouseGuiLayout.SECOND_PERFORMANCE_Y,
                ModuleKind.PERFORMANCE
        ));
        blockEntity.runtime().battery().ifPresent(battery -> addSlot(new BatterySlot(
                battery.items(), 0, GreenhouseGuiLayout.BATTERY_X, GreenhouseGuiLayout.BATTERY_Y
        )));

        addPlayerInventory(
                GreenhouseGuiLayout.PLAYER_INVENTORY_X,
                GreenhouseGuiLayout.PLAYER_INVENTORY_Y,
                GreenhouseGuiLayout.PLAYER_HOTBAR_Y
        );
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem module) {
            return switch (module.kind()) {
                case PROCESS -> moveToMachineSlot(stack, PROCESS_MODULE_MENU_SLOT);
                case PERFORMANCE -> moveToMachineSlots(
                        stack, FIRST_PERFORMANCE_MENU_SLOT, PERFORMANCE_MENU_SLOT_END
                );
                case PROTECTION -> false;
            };
        }
        if (BatterySlotComponent.isValidBatteryItem(stack)) {
            return moveToMachineSlot(stack, BATTERY_MENU_SLOT);
        }
        return moveToMachineSlots(stack, FIRST_INPUT_MENU_SLOT, INPUT_MENU_SLOT_END);
    }

    @Override public BlockPos machinePos() { return blockEntity.getBlockPos(); }

    public int progress() { return dataValue(0); }
    public int maxProgress() { return dataValue(1); }
    public int energy() { return int32DataValue(2, 15); }
    public int energyCapacity() { return int32DataValue(3, 16); }
    public int activeEnergyPerTick() { return dataValue(4); }

    public int waterAmount() { return int32DataValue(6, 17); }
    public int waterCapacity() { return int32DataValue(7, 18); }
    public FluidStack waterStack() { return fluidStack(dataValue(8), waterAmount()); }

    public int nutrientFluidAmount() { return int32DataValue(9, 19); }
    public int nutrientFluidCapacity() { return int32DataValue(10, 20); }
    public FluidStack nutrientFluidStack() { return fluidStack(dataValue(11), nutrientFluidAmount()); }

    public int outputFluidAmount() { return int32DataValue(12, 21); }
    public int outputFluidCapacity() { return int32DataValue(13, 22); }
    public FluidStack outputFluidStack() { return fluidStack(dataValue(14), outputFluidAmount()); }

    private static FluidStack fluidStack(int registryId, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.byId(registryId & 0xFFFF);
        return fluid == null || fluid == Fluids.EMPTY || amount <= 0
                ? FluidStack.EMPTY
                : new FluidStack(fluid, amount);
    }

    private static GreenhouseBlockEntity requireBlockEntity(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof GreenhouseBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Greenhouse block entity is missing at " + pos);
    }
}
