package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.AdsorptionColumnControllerBlock;
import org.kdvcs.poiesis.block.entity.base.AbstractMultiblockControllerBlockEntity;
import org.kdvcs.poiesis.machine.AdsorptionColumnRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.machine.io.fluid.RoutedFluidHandler;
import org.kdvcs.poiesis.multiblock.AdsorptionColumnStructure;
import org.kdvcs.poiesis.multiblock.AdsorptionColumnVisualController;
import org.kdvcs.poiesis.multiblock.DirectionalCapabilityCache;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.screen.AdsorptionColumnMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

import java.util.EnumSet;
import java.util.Objects;


public final class AdsorptionColumnControllerBlockEntity
        extends AbstractMultiblockControllerBlockEntity<
        AdsorptionColumnRuntime,
        AdsorptionColumnStructure.ValidationResult
        > {
    public static final int FLUID_CAPACITY = 48_000;
    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "adsorption_column",
            new EnergySpec(128_000, 1_280, 40, 80, 640, 20),
            1,
            1,
            0,
            MachineIoSpec.builder()
                    .item()
                    .channel(
                            IoChannel.FLUID,
                            EnumSet.of(SideIoMode.NONE, SideIoMode.OUTPUT),
                            SideIoMode.OUTPUT,
                            false,
                            true
                    )
                    .energyInput()
                    .build(),
            new BatterySpec(800),
            new FluidSpec(FLUID_CAPACITY)
    );

    private final DirectionalCapabilityCache<IFluidHandler> cleanFluidCapabilities =
            new DirectionalCapabilityCache<>();

    @Override
    protected SoundEvent workingSound() {
        return ModSounds.ADSORBER_WORKING.get();
    }

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var process = runtime().process();
            var input = runtime().fluid().orElseThrow();
            var output = runtime().outputFluid();
            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> runtime().energy().stored();
                case 3 -> runtime().energy().capacity();
                case 4 -> process.activeEnergyPerTick();
                case 5 -> process.activeVoltage();
                case 6 -> process.state().ordinal();
                case 7 -> input.amount();
                case 8 -> input.capacity();
                case 9 -> fluidId(input.fluid());
                case 10 -> output.amount();
                case 11 -> output.capacity();
                case 12 -> fluidId(output.fluid());
                case 13 -> bedCount();
                case 14 -> MenuDataCodec.highWord(runtime().energy().stored());
                case 15 -> MenuDataCodec.highWord(runtime().energy().capacity());
                case 16 -> MenuDataCodec.highWord(input.amount());
                case 17 -> MenuDataCodec.highWord(input.capacity());
                case 18 -> MenuDataCodec.highWord(output.amount());
                case 19 -> MenuDataCodec.highWord(output.capacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return AdsorptionColumnMenu.DATA_COUNT;
        }
    };

    public AdsorptionColumnControllerBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.ADSORPTION_COLUMN_CONTROLLER.get(),
                pos,
                state,
                DEFINITION,
                owner -> {
                    AdsorptionColumnControllerBlockEntity controller =
                            (AdsorptionColumnControllerBlockEntity) owner;
                    return new AdsorptionColumnRuntime(
                            owner,
                            DEFINITION,
                            ModRecipeTypes.ADSORPTION.get(),
                            controller::isFormed,
                            controller::bedCount
                    );
                },
                AdsorptionColumnStructure.DEFINITION
        );
    }

    private static int fluidId(FluidStack stack) {
        return BuiltInRegistries.FLUID.getId(
                stack.isEmpty() ? Fluids.EMPTY : stack.getFluid()
        );
    }

    public int bedCount() {
        return structureState().bedCount();
    }

    public FluidTank dirtyInputTank() {
        return runtime().fluid().orElseThrow().tank();
    }

    public FluidTank cleanOutputTank() {
        return runtime().outputFluid().tank();
    }

    /** Clears the remaining part visuals without placing the controller back. */
    public void dismantleForControllerRemoval() {
        if (level == null || level.isClientSide) {
            return;
        }

        AdsorptionColumnStructure.ValidationResult previous = clearStructureStateForRemoval();

        updatePartVisualStates(previous.bedCount(), previous.distributorPos(), false, false);
        refreshDistributor(previous.distributorPos());
    }

    @Override
    protected void onStructureStateChanged(
            AdsorptionColumnStructure.ValidationResult previous,
            AdsorptionColumnStructure.ValidationResult result
    ) {
        boolean wasFormed = previous.formed();
        int oldBedCount = previous.bedCount();
        BlockPos oldDistributorPos = previous.distributorPos();
        BlockState controllerState = getBlockState();
        boolean working = result.formed()
                && controllerState.getValue(AdsorptionColumnControllerBlock.WORKING);
        boolean changed = wasFormed != result.formed()
                || oldBedCount != result.bedCount()
                || !Objects.equals(oldDistributorPos, result.distributorPos());

        if (changed) {
            updatePartVisualStates(oldBedCount, oldDistributorPos, false, false);
        }

        if (result.formed()) {
            int structureCapacity = result.bedCount() * AdsorptionRecipe.FLUID_PER_BED;
            dirtyInputTank().setCapacity(Math.max(
                    structureCapacity,
                    dirtyInputTank().getFluidAmount()
            ));
            cleanOutputTank().setCapacity(Math.max(
                    structureCapacity,
                    cleanOutputTank().getFluidAmount()
            ));
        }

        BlockState visualState = getBlockState()
                .setValue(AdsorptionColumnControllerBlock.FORMED, result.formed())
                .setValue(AdsorptionColumnControllerBlock.WORKING, working);
        if (!visualState.equals(getBlockState())) {
            level.setBlock(worldPosition, visualState, 2);
        }
        if (result.formed()) {
            updatePartVisualStates(result.bedCount(), result.distributorPos(), true, working);
        }

        if (!changed) {
            return;
        }

        refreshCapabilityViews();
        refreshDistributor(oldDistributorPos);
        refreshDistributor(result.distributorPos());
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());

        if (!wasFormed && result.formed() && level instanceof ServerLevel serverLevel) {
            AdsorptionColumnVisualController.emitFormationParticles(
                    serverLevel, worldPosition, result.bedCount()
            );
        }
    }

    @Override
    protected void onWorkingStateChanged(boolean working) {
        if (isFormed()) {
            updatePartVisualStates(
                    bedCount(), structureState().distributorPos(), true, working
            );
        }
    }

    private void updatePartVisualStates(
            int targetBedCount,
            @Nullable BlockPos targetDistributorPos,
            boolean targetFormed,
            boolean targetWorking
    ) {
        if (level != null) {
            AdsorptionColumnVisualController.updateParts(
                    level,
                    worldPosition,
                    targetBedCount,
                    targetDistributorPos,
                    targetFormed,
                    targetWorking
            );
        }
    }

    private void refreshDistributor(@Nullable BlockPos pos) {
        if (pos != null && level != null
                && level.getBlockEntity(pos) instanceof FluidDistributorBlockEntity distributor) {
            distributor.refreshCapabilityView();
        }
    }

    public boolean ownsDistributor(BlockPos pos) {
        return isFormed() && pos.equals(structureState().distributorPos());
    }

    public SimpleContainer inventoryForDrops() {
        return runtime().allDrops();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AdsorptionColumnMenu(id, inventory, this, data);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.poiesis.adsorber");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER
                || capability == ForgeCapabilities.ENERGY) {
            if (!isFormed() || side == null || !side.getAxis().isHorizontal()) {
                return LazyOptional.empty();
            }
            return super.getCapability(capability, side);
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (!isFormed() || side == null || !side.getAxis().isHorizontal()) {
                return LazyOptional.empty();
            }
            RelativeSide relativeSide = RelativeSide.fromWorldSide(side, facing());
            if (!runtime().sideConfiguration().orElseThrow()
                    .allowsOutput(IoChannel.FLUID, relativeSide)) {
                return LazyOptional.empty();
            }
            return cleanFluidCapabilities.get(
                    side,
                    worldSide -> new RoutedFluidHandler(
                            cleanOutputTank(),
                            this::isFormed,
                            () -> false,
                            () -> runtime().sideConfiguration().orElseThrow()
                                    .allowsOutput(
                                            IoChannel.FLUID,
                                            RelativeSide.fromWorldSide(worldSide, facing())
                                    )
                    )
            ).cast();
        }
        return super.getCapability(capability, side);
    }

    private void refreshCapabilityViews() {
        cleanFluidCapabilities.invalidate();
        runtime().refreshIoCapabilities();
    }

    @Override
    public void invalidateCaps() {
        cleanFluidCapabilities.invalidate();
        super.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
    }
}
