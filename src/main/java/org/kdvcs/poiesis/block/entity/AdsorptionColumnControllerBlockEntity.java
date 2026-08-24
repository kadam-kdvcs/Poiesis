package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.AdsorptionBedBlock;
import org.kdvcs.poiesis.block.AdsorptionColumnControllerBlock;
import org.kdvcs.poiesis.block.FluidDistributorBlock;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
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
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.screen.AdsorptionColumnMenu;

import java.util.EnumMap;
import java.util.EnumSet;


public final class AdsorptionColumnControllerBlockEntity
        extends AbstractMachineBlockEntity<AdsorptionColumnRuntime> {
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

    private final EnumMap<Direction, LazyOptional<IFluidHandler>> cleanFluidCapabilities =
            new EnumMap<>(Direction.class);

    private boolean formed;
    private int bedCount;
    @Nullable
    private BlockPos distributorPos;

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
                case 13 -> bedCount;
                case 14 -> runtime().energy().stored() >>> 16;
                case 15 -> runtime().energy().capacity() >>> 16;
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
                }
        );
        createCapabilityViews();
    }

    private static int fluidId(net.minecraftforge.fluids.FluidStack stack) {
        return BuiltInRegistries.FLUID.getId(
                stack.isEmpty() ? Fluids.EMPTY : stack.getFluid()
        );
    }

    public boolean isFormed() {
        return formed;
    }

    public int bedCount() {
        return bedCount;
    }

    public FluidTank dirtyInputTank() {
        return runtime().fluid().orElseThrow().tank();
    }

    public FluidTank cleanOutputTank() {
        return runtime().outputFluid().tank();
    }

    public void validateStructure() {
        if (level != null && !level.isClientSide) {
            applyStructureState(AdsorptionColumnStructure.validate(level, worldPosition));
        }
    }

    public void unform() {
        applyStructureState(AdsorptionColumnStructure.ValidationResult.invalid());
    }

    /** Clears the remaining part visuals without placing the controller back. */
    public void dismantleForControllerRemoval() {
        if (level == null || level.isClientSide) {
            return;
        }

        int oldBedCount = bedCount;
        BlockPos oldDistributorPos = distributorPos;
        formed = false;
        bedCount = 0;
        distributorPos = null;

        updatePartVisualStates(oldBedCount, oldDistributorPos, false, false);
        refreshDistributor(oldDistributorPos);
    }

    public void applyStructureState(AdsorptionColumnStructure.ValidationResult result) {
        if (level == null || level.isClientSide) {
            return;
        }

        boolean wasFormed = formed;
        int oldBedCount = bedCount;
        BlockPos oldDistributorPos = distributorPos;
        BlockState controllerState = getBlockState();
        boolean working = result.formed()
                && controllerState.getValue(AdsorptionColumnControllerBlock.WORKING);
        boolean changed = wasFormed != result.formed()
                || oldBedCount != result.bedCount()
                || !java.util.Objects.equals(oldDistributorPos, result.distributorPos());

        if (changed) {
            updatePartVisualStates(oldBedCount, oldDistributorPos, false, false);
        }

        formed = result.formed();
        bedCount = result.bedCount();
        distributorPos = result.distributorPos();
        if (formed) {
            int structureCapacity = bedCount * AdsorptionRecipe.FLUID_PER_BED;
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
                .setValue(AdsorptionColumnControllerBlock.FORMED, formed)
                .setValue(AdsorptionColumnControllerBlock.WORKING, working);
        if (!visualState.equals(getBlockState())) {
            level.setBlock(worldPosition, visualState, 2);
        }
        if (formed) {
            updatePartVisualStates(bedCount, distributorPos, true, working);
        }

        if (!changed) {
            return;
        }

        refreshCapabilityViews();
        refreshDistributor(oldDistributorPos);
        refreshDistributor(distributorPos);
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());

        if (!wasFormed && formed && level instanceof ServerLevel serverLevel) {
            emitFormationParticles(serverLevel);
        }
    }

    @Override
    protected void onWorkingStateChanged(boolean working) {
        if (formed) {
            updatePartVisualStates(bedCount, distributorPos, true, working);
        }
    }

    private void updatePartVisualStates(
            int targetBedCount,
            @Nullable BlockPos targetDistributorPos,
            boolean targetFormed,
            boolean targetWorking
    ) {
        if (level == null) {
            return;
        }
        for (int index = 1; index <= targetBedCount; index++) {
            BlockPos bedPos = worldPosition.above(index);
            BlockState bedState = level.getBlockState(bedPos);
            if (!bedState.is(ModBlocks.ADSORPTION_BED.get())) {
                continue;
            }
            BlockState next = bedState
                    .setValue(AdsorptionBedBlock.FORMED, targetFormed)
                    .setValue(AdsorptionBedBlock.BOTTOM, targetFormed && index == 1)
                    .setValue(AdsorptionBedBlock.WORKING, targetFormed && targetWorking);
            if (!next.equals(bedState)) {
                level.setBlock(bedPos, next, 2);
            }
        }
        if (targetDistributorPos != null) {
            BlockState state = level.getBlockState(targetDistributorPos);
            if (state.is(ModBlocks.FLUID_DISTRIBUTOR.get())) {
                BlockState next = state.setValue(FluidDistributorBlock.FORMED, targetFormed);
                if (!next.equals(state)) {
                    level.setBlock(targetDistributorPos, next, 2);
                }
            }
        }
    }

    private void emitFormationParticles(ServerLevel serverLevel) {
        int height = bedCount + 2;
        for (int offset = 0; offset < height; offset++) {
            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + offset + 0.5D;
            double z = worldPosition.getZ() + 0.5D;
            serverLevel.sendParticles(DustParticleOptions.REDSTONE, x - 0.58D, y, z,
                    8, 0.08D, 0.46D, 0.46D, 0.0D);
            serverLevel.sendParticles(DustParticleOptions.REDSTONE, x + 0.58D, y, z,
                    8, 0.08D, 0.46D, 0.46D, 0.0D);
            serverLevel.sendParticles(DustParticleOptions.REDSTONE, x, y, z - 0.58D,
                    8, 0.46D, 0.46D, 0.08D, 0.0D);
            serverLevel.sendParticles(DustParticleOptions.REDSTONE, x, y, z + 0.58D,
                    8, 0.46D, 0.46D, 0.08D, 0.0D);
        }
    }

    private void refreshDistributor(@Nullable BlockPos pos) {
        if (pos != null && level != null
                && level.getBlockEntity(pos) instanceof FluidDistributorBlockEntity distributor) {
            distributor.refreshCapabilityView();
        }
    }

    public boolean ownsDistributor(BlockPos pos) {
        return formed && pos.equals(distributorPos);
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("formed", formed);
        tag.putInt("bed_count", bedCount);
        if (distributorPos != null) {
            tag.putLong("distributor_pos", distributorPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        formed = tag.getBoolean("formed");
        bedCount = tag.getInt("bed_count");
        distributorPos = tag.contains("distributor_pos")
                ? BlockPos.of(tag.getLong("distributor_pos"))
                : null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER
                || capability == ForgeCapabilities.ENERGY) {
            if (!formed || side == null || !side.getAxis().isHorizontal()) {
                return LazyOptional.empty();
            }
            return super.getCapability(capability, side);
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (!formed || side == null || !side.getAxis().isHorizontal()) {
                return LazyOptional.empty();
            }
            RelativeSide relativeSide = RelativeSide.fromWorldSide(side, facing());
            if (!runtime().sideConfiguration().orElseThrow()
                    .allowsOutput(IoChannel.FLUID, relativeSide)) {
                return LazyOptional.empty();
            }
            return cleanFluidCapabilities.computeIfAbsent(
                    side,
                    worldSide -> LazyOptional.of(() -> new RoutedFluidHandler(
                            cleanOutputTank(),
                            () -> formed,
                            () -> false,
                            () -> runtime().sideConfiguration().orElseThrow()
                                    .allowsOutput(
                                            IoChannel.FLUID,
                                            RelativeSide.fromWorldSide(worldSide, facing())
                                    )
                    ))
            ).cast();
        }
        return super.getCapability(capability, side);
    }

    private void createCapabilityViews() {
        cleanFluidCapabilities.clear();
    }

    private void refreshCapabilityViews() {
        cleanFluidCapabilities.values().forEach(LazyOptional::invalidate);
        createCapabilityViews();
        runtime().refreshIoCapabilities();
    }

    @Override
    public void invalidateCaps() {
        cleanFluidCapabilities.values().forEach(LazyOptional::invalidate);
        cleanFluidCapabilities.clear();
        super.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        createCapabilityViews();
    }
}
