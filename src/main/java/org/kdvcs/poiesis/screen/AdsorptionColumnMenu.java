package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.AdsorptionColumnGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class AdsorptionColumnMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 20;
    private static final int INPUT_MENU_SLOT = 0;
    private static final int OUTPUT_MENU_SLOT = 1;
    private static final int BATTERY_MENU_SLOT = 2;
    private static final int MACHINE_SLOT_COUNT = 3;

    private final AdsorptionColumnControllerBlockEntity blockEntity;

    public AdsorptionColumnMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                id,
                inventory,
                requireBlockEntity(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public AdsorptionColumnMenu(
            int id,
            Inventory playerInventory,
            AdsorptionColumnControllerBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.ADSORPTION_COLUMN.get(),
                id,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var items = blockEntity.runtime().inventory().items();
        addSlot(new SlotItemHandler(
                items,
                0,
                AdsorptionColumnGuiLayout.ADSORBENT_X,
                AdsorptionColumnGuiLayout.ADSORBENT_Y
        ));
        addSlot(new OutputSlot(
                items,
                1,
                AdsorptionColumnGuiLayout.RESULT_X,
                AdsorptionColumnGuiLayout.RESULT_Y
        ));
        blockEntity.runtime().battery().ifPresent(battery -> addSlot(new BatterySlot(
                battery.items(),
                0,
                AdsorptionColumnGuiLayout.BATTERY_X,
                AdsorptionColumnGuiLayout.BATTERY_Y
        )));

        addPlayerInventory(
                AdsorptionColumnGuiLayout.PLAYER_INVENTORY_X,
                AdsorptionColumnGuiLayout.PLAYER_INVENTORY_Y
        );
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (BatterySlotComponent.isValidBatteryItem(stack)
                && moveToMachineSlot(stack, BATTERY_MENU_SLOT)) {
            return true;
        }
        return moveToMachineSlot(stack, INPUT_MENU_SLOT);
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.isFormed() && super.stillValid(player);
    }

    public int progress() { return dataValue(0); }
    public int maxProgress() { return dataValue(1); }
    public int energy() {
        return int32DataValue(2, 14);
    }
    public int energyCapacity() {
        return int32DataValue(3, 15);
    }
    public int activeEnergyPerTick() { return dataValue(4); }
    public int stateOrdinal() { return dataValue(6); }
    public int inputFluidAmount() { return int32DataValue(7, 16); }
    public int inputFluidCapacity() { return int32DataValue(8, 17); }
    public int outputFluidAmount() { return int32DataValue(10, 18); }
    public int outputFluidCapacity() { return int32DataValue(11, 19); }
    public int bedCount() { return dataValue(13); }

    public FluidStack inputFluidStack() {
        return fluidStack(dataValue(9), inputFluidAmount());
    }

    public FluidStack outputFluidStack() {
        return fluidStack(dataValue(12), outputFluidAmount());
    }

    private static FluidStack fluidStack(int registryId, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.byId(registryId & 0xFFFF);
        if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    private static AdsorptionColumnControllerBlockEntity requireBlockEntity(
            Inventory inventory,
            BlockPos pos
    ) {
        if (inventory.player.level().getBlockEntity(pos)
                instanceof AdsorptionColumnControllerBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Adsorption column controller is missing at " + pos);
    }
}
