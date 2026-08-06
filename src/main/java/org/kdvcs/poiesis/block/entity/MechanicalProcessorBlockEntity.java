package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.MechanicalProcessorBlock;
import org.kdvcs.poiesis.machine.MechanicalMachineRuntime;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.MechanicalProcessorMenu;

public final class MechanicalProcessorBlockEntity
        extends BlockEntity
        implements MenuProvider {

    public static final MachineDefinition DEFINITION =
            new MachineDefinition(
                    "mechanical_processor",
                    new EnergySpec(
                            40_000,
                            640,
                            20,
                            40,
                            320,
                            20
                    ),
                    1,
                    1,
                    3
            );

    private final MechanicalMachineRuntime runtime =
            new MechanicalMachineRuntime(this, DEFINITION);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> runtime.process().progress();
                case 1 -> runtime.process().maxProgress();
                case 2 -> runtime.energy().stored();
                case 3 -> runtime.energy().capacity();
                case 4 -> runtime.process().activeEnergyPerTick();
                case 5 -> runtime.process().activeVoltage();
                case 6 -> runtime.process().state().ordinal();
                case 7 -> runtime.modules().currentMode().ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public MechanicalProcessorBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.MECHANICAL_PROCESSOR.get(),
                pos,
                state
        );
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            MechanicalProcessorBlockEntity blockEntity
    ) {
        blockEntity.runtime.tickServer();
    }

    public MechanicalMachineRuntime runtime() {
        return runtime;
    }

    public void setWorking(boolean working) {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();

        if (state.hasProperty(MechanicalProcessorBlock.WORKING)
                && state.getValue(
                        MechanicalProcessorBlock.WORKING
                ) != working) {
            level.setBlock(
                    worldPosition,
                    state.setValue(
                            MechanicalProcessorBlock.WORKING,
                            working
                    ),
                    3
            );
        }
    }

    public SimpleContainer allDrops() {
        int inventorySlots =
                runtime.inventory().items().getSlots();

        int moduleSlots =
                runtime.modules().items().getSlots();

        SimpleContainer drops =
                new SimpleContainer(
                        inventorySlots + moduleSlots
                );

        int index = 0;

        for (int slot = 0; slot < inventorySlots; slot++) {
            drops.setItem(
                    index++,
                    runtime.inventory()
                            .items()
                            .getStackInSlot(slot)
            );
        }

        for (int slot = 0; slot < moduleSlots; slot++) {
            drops.setItem(
                    index++,
                    runtime.modules()
                            .items()
                            .getStackInSlot(slot)
            );
        }

        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        runtime.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        runtime.load(tag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        runtime.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return runtime.inventory()
                    .capability()
                    .cast();
        }

        if (capability == ForgeCapabilities.ENERGY) {
            return runtime.energy()
                    .capability()
                    .cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public Component getDisplayName() {
        return ModBlocks.MECHANICAL_PROCESSOR
                .get()
                .getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int id,
            Inventory inventory,
            Player player
    ) {
        return new MechanicalProcessorMenu(
                id,
                inventory,
                this,
                data
        );
    }
}
