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
import org.kdvcs.poiesis.block.entity.BiomassGasifierBlockEntity;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.layout.BiomassGasifierGuiLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class BiomassGasifierMenu extends AbstractMachineMenu {
    private static final int INPUT_MENU_SLOT = 0;
    private static final int MACHINE_SLOT_COUNT = 2;

    public static final int DATA_COUNT = 8;

    private final BiomassGasifierBlockEntity blockEntity;

    public BiomassGasifierMenu(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                requireBlockEntity(playerInventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public BiomassGasifierMenu(
            int containerId,
            Inventory playerInventory,
            BiomassGasifierBlockEntity blockEntity,
            ContainerData data
    ) {
        super(
                ModMenuTypes.BIOMASS_GASIFIER.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.BIOMASS_GASIFIER.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        checkContainerDataCount(data, DATA_COUNT);

        var inventory = blockEntity.runtime().inventory().items();
        addSlot(new SlotItemHandler(
                inventory,
                0,
                BiomassGasifierGuiLayout.INPUT_X,
                BiomassGasifierGuiLayout.INPUT_Y
        ));
        addSlot(new OutputSlot(
                inventory,
                1,
                BiomassGasifierGuiLayout.OUTPUT_X,
                BiomassGasifierGuiLayout.OUTPUT_Y
        ));

        addPlayerInventory(8, 90);
        addDataSlots(data);
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        return moveToMachineSlot(stack, INPUT_MENU_SLOT);
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    public int progress() {
        return dataValue(0);
    }

    public int maxProgress() {
        return dataValue(1);
    }

    public int temperature() {
        return dataValue(2);
    }

    public int maximumTemperature() {
        return dataValue(3);
    }

    public int gasAmount() {
        return dataValue(4) & 0xFFFF;
    }

    public int gasCapacity() {
        return dataValue(5) & 0xFFFF;
    }

    public FluidStack gasStack() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(dataValue(6) & 0xFFFF);
        if (fluid == null || fluid == Fluids.EMPTY || gasAmount() <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, gasAmount());
    }

    public int stateOrdinal() {
        return dataValue(7);
    }

    private static BiomassGasifierBlockEntity requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player.level().getBlockEntity(pos)
                instanceof BiomassGasifierBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Biomass gasifier is missing at " + pos);
    }
}
